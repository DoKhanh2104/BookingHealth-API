package com.bookinghealth.api.dto.response.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SatisfactionReportResponse {
  Long id; // Doctor ID
  String doctorName;
  String specialtyName;
  Long totalReviews;
  Double averageRating;
  Long negativeReviews;
}
