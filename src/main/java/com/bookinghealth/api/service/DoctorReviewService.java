package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.client.CreateReviewRequest;
import com.bookinghealth.api.dto.response.client.DoctorReviewResponse;
import com.bookinghealth.api.entity.Appointment;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorReview;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.AppointmentRepository;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.DoctorReviewRepository;
import com.bookinghealth.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorReviewService {

  DoctorReviewRepository doctorReviewRepository;
  AppointmentRepository appointmentRepository;
  DoctorRepository doctorRepository;
  UserRepository userRepository;

  @Transactional
  public DoctorReviewResponse createReview(CreateReviewRequest request) {
    User currentUser = getCurrentUser();

    Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
        .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

    Doctor doctor = doctorRepository.findById(request.getDoctorId())
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

    // Verify appointment belongs to user
    if (appointment.getUser() == null || !appointment.getUser().getId().equals(currentUser.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Verify appointment status is completed (2)
    if (appointment.getStatus() == null || appointment.getStatus() != 2) {
      throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
    }

    // Prevent spam: Check if already reviewed
    if (doctorReviewRepository.existsByAppointmentId(appointment.getId())) {
      throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
    }

    DoctorReview review = DoctorReview.builder()
        .rating(request.getRating())
        .comment(request.getComment())
        .doctor(doctor)
        .user(currentUser)
        .appointment(appointment)
        .build();

    DoctorReview saved = doctorReviewRepository.save(review);

    return DoctorReviewResponse.builder()
        .id(saved.getId())
        .patientName(currentUser.getName())
        .rating(saved.getRating())
        .comment(saved.getComment())
        .date(appointment.getExpectedExaminationDate() != null ? appointment.getExpectedExaminationDate().toString() : "")
        .build();
  }

  private User getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    String identifier = authentication.getName();
    return userRepository.findByEmail(identifier)
        .or(() -> userRepository.findByPhone(identifier))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
  }
}
