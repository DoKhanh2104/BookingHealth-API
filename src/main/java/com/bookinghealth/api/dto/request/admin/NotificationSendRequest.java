package com.bookinghealth.api.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationSendRequest {
  @NotBlank(message = "Title is required")
  String title;

  @NotBlank(message = "Content is required")
  String content;

  @NotNull(message = "Type is required")
  Integer type; // 1=Appointment, 2=System, 3=Reminder

  @NotBlank(message = "Target is required")
  String target; // ALL, DOCTOR, PATIENT
}
