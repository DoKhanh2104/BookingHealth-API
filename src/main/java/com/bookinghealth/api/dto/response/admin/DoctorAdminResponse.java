package com.bookinghealth.api.dto.response.admin;

import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
