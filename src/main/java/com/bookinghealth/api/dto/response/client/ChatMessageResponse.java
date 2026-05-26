package com.bookinghealth.api.dto.response.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
  Long id;
  Long chatRoomId;
  Long senderId;
  String senderName;
  String senderAvatar;
  String content;
  String sendTime;
}
