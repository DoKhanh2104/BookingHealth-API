package com.bookinghealth.api.dto.response.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomResponse {
  Long id;
  Long appointmentId;
  Long doctorId;
  String doctorName;
  String doctorAvatar;
  Long userId;
  String userName;
  String userAvatar;
  Integer status;
  String lastMessage;
  String lastMessageTime;
}
