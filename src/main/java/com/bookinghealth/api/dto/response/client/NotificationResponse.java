package com.bookinghealth.api.dto.response.client;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
  Long id;
  Long userId;
  String title;
  String content;
  Integer type;
  Boolean isRead;
  LocalDateTime createdAt;
}
