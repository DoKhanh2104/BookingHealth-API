package com.bookinghealth.api.dto.response.admin;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentResponse {

  String patientName;
  String patientPhone;
  String doctorName;
  String specialty;
  String appointmentDate;
  Double appointmentFee;
  String status;
  String timeSlot;
}
