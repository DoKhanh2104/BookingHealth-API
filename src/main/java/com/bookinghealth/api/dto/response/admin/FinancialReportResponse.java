package com.bookinghealth.api.dto.response.admin;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialReportResponse {
  Long appointmentId;
  String patientName;
  String doctorName;
  Double amount;
  String paymentMethod;
  LocalDateTime paymentTime;
}
