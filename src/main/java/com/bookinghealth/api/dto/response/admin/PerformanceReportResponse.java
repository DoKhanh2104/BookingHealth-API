package com.bookinghealth.api.dto.response.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PerformanceReportResponse {
  Long id; // Doctor ID
  String doctorOrSpecialtyName;
  Long total;
  Long completed;
  Long cancelled;
  Double cancelRate;
}
