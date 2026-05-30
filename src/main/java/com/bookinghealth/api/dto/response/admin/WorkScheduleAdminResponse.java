package com.bookinghealth.api.dto.response.admin;

import java.time.LocalDate;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleAdminResponse {
  Long id;
  Long doctorId;
  String doctorName;
  Long clinicId;
  String clinicName;
  LocalDate date;
  List<String> timeSlots;
}
