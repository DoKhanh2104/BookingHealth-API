package com.bookinghealth.api.dto.request.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualificationRequest {
  String degree;
  String issueDate;
  String attachmentUrl;
}
