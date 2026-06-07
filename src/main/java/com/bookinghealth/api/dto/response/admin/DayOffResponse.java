package com.bookinghealth.api.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DayOffResponse {
  Long id;
  Long doctorId;
  String doctorName;
  String clinicName;

  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate startDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate endDate;

  String reason;
  Integer status; // 0=PENDING, 1=APPROVED, 2=REJECTED
}
