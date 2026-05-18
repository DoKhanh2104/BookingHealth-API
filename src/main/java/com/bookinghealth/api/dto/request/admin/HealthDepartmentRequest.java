package com.bookinghealth.api.dto.request.admin;

import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthDepartmentRequest {

  String id;

  LocalDate issuedDate;

  String facilityType;

  String organizationType;

  String medicalFacilityName;

  String address;

  String district;

  String ward;

  String phoneNumber;

  String doctorName;
}
