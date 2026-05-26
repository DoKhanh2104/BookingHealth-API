package com.bookinghealth.api.dto.response.client;

import com.bookinghealth.api.dto.response.UserResponse;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentResponse {
  Long id;
  Integer status;
  String description;
  LocalDate expectedExaminationDate;
  LocalDate actualArrivalTime;
  Double totalAmount;
  String paymentMethod;
  Integer paymentStatus;
  String diagnosis;
  String medicine;
  String attachment;
  DoctorResponse doctor;
  UserResponse user;
  ScheduleSlotResponse appointmentSlot;
  java.util.List<DoctorReviewResponse> reviews;
}
