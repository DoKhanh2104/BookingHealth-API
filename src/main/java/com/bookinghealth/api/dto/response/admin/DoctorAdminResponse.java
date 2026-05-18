package com.bookinghealth.api.dto.response.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorAdminResponse {

  Long id;
  String doctorName;
  String email;
  String phoneNumber;
  String clinicName;
  String licenseNumber;
  Integer status;
  String specialtyName;
  LocalDate practiceStartDate;
  String avatar;
}
