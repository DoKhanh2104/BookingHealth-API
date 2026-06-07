package com.bookinghealth.api.mapper;

import com.bookinghealth.api.dto.response.admin.ClinicAdminResponse;
import com.bookinghealth.api.dto.response.client.DoctorResponse;
import com.bookinghealth.api.dto.response.client.DoctorReviewResponse;
import com.bookinghealth.api.dto.response.client.QualificationResponse;
import com.bookinghealth.api.entity.Clinic;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorReview;
import com.bookinghealth.api.entity.PriceHistory;
import com.bookinghealth.api.entity.Qualification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {SpecialtyMapper.class})
public interface DoctorMapper {

  @Mapping(source = "user.name", target = "name")
  @Mapping(source = "user.phone", target = "phone")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.avatar", target = "avatar")
  @Mapping(source = "clinic", target = "clinic")
  @Mapping(source = "specialties", target = "specialties")
  @Mapping(source = "qualifications", target = "qualifications")
  @Mapping(source = "reviews", target = "reviews")
  @Mapping(target = "yearsOfExperience", expression = "java(calculateYearsOfExperience(doctor))")
  @Mapping(target = "averageRating", expression = "java(calculateAverageRating(doctor))")
  @Mapping(target = "reviewCount", expression = "java(calculateReviewCount(doctor))")
  @Mapping(target = "examinationFee", expression = "java(calculateExaminationFee(doctor))")
  DoctorResponse toDoctorResponse(Doctor doctor);

  @Mapping(source = "qualificationName", target = "degree")
  QualificationResponse toQualificationResponse(Qualification qualification);

  @Mapping(source = "user.name", target = "patientName")
  @Mapping(target = "date", expression = "java(getReviewDate(review))")
  DoctorReviewResponse toDoctorReviewResponse(DoctorReview review);

  default String getReviewDate(DoctorReview review) {
    if (review.getAppointment() != null
        && review.getAppointment().getExpectedExaminationDate() != null) {
      return review.getAppointment().getExpectedExaminationDate().toString();
    }
    return java.time.LocalDate.now().toString();
  }

  default ClinicAdminResponse mapClinic(Clinic clinic) {
    if (clinic == null) {
      return null;
    }
    return ClinicAdminResponse.builder()
        .id(clinic.getId())
        .clinicName(clinic.getClinicName())
        .address(clinic.getAddress())
        .longitude(clinic.getLongitude())
        .latitude(clinic.getLatitude())
        .soLuongBacSi(clinic.getDoctors() != null ? clinic.getDoctors().size() : 0)
        .build();
  }

  default Integer calculateYearsOfExperience(Doctor doctor) {
    if (doctor.getPracticeStartDate() == null) {
      return 0;
    }
    return java.time.LocalDate.now().getYear() - doctor.getPracticeStartDate().getYear();
  }

  default Double calculateAverageRating(Doctor doctor) {
    if (doctor.getReviews() == null || doctor.getReviews().isEmpty()) {
      return 5.0;
    }
    double sum =
        doctor.getReviews().stream()
            .mapToDouble(r -> r.getRating() != null ? r.getRating() : 5.0)
            .sum();
    return Math.round((sum / doctor.getReviews().size()) * 10.0) / 10.0;
  }

  default Integer calculateReviewCount(Doctor doctor) {
    return doctor.getReviews() != null ? doctor.getReviews().size() : 0;
  }

  default Double calculateExaminationFee(Doctor doctor) {
    if (doctor.getPriceHistories() == null || doctor.getPriceHistories().isEmpty()) {
      return 150000.0; // Default standard consultation fee
    }
    return doctor.getPriceHistories().stream()
        .filter(ph -> ph.getStatus() != null && ph.getStatus() == 1)
        .map(PriceHistory::getExaminationFee)
        .findFirst()
        .orElse(150000.0);
  }
}
