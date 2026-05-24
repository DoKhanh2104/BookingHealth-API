package com.bookinghealth.api.dto.response.client;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualificationResponse {
  Long id;
  String degree; // Maps to qualificationName
  LocalDateTime issueDate;
}
