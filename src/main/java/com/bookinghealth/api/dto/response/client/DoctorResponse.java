package com.bookinghealth.api.dto.response.client;

import com.bookinghealth.api.dto.response.admin.ClinicAdminResponse;
import com.bookinghealth.api.dto.response.admin.SpecialtyResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorResponse {
  Long id;
  String biography;
  LocalDate practiceStartDate;
  String practiceLicenseNumber;
  String practiceLicenseImage;
  Integer status;

  // User fields
  String name;
  String phone;
  String email;
  String avatar;

  // Clinic and Specialties
  ClinicAdminResponse clinic;
  Set<SpecialtyResponse> specialties;

  List<QualificationResponse> qualifications;
  List<DoctorReviewResponse> reviews;

  // Derived fields
  Integer yearsOfExperience;
  Double averageRating;
  Integer reviewCount;
  Double examinationFee;
}
