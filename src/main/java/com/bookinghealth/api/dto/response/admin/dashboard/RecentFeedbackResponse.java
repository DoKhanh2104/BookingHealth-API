package com.bookinghealth.api.dto.response.admin.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecentFeedbackResponse {

  Long id;
  String patientName;
  String doctorName;
  int rating;
  String comment;

  /** Thời gian hiển thị dạng "10 phút trước" */
  String date;
}
