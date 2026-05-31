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
public class NotificationAdminResponse {
  Long id;
  String title;
  String content;
  Integer type;
  LocalDateTime createdAt;
  Integer status; // 0: Unread, 1: Read
  Long userId;
  String userName;
  String userEmail;
}
