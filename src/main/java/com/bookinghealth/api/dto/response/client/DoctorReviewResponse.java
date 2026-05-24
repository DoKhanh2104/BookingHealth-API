package com.bookinghealth.api.dto.response.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorReviewResponse {
  Long id;
  String patientName; // Maps to user.name
  Integer rating;
  String comment;
  String date; // Maps to appointment expectedExaminationDate
}
