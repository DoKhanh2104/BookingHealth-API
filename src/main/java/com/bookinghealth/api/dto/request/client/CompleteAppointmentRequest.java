package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteAppointmentRequest {

  @NotBlank(message = "Chẩn đoán y khoa không được để trống")
  String diagnosis;

  String medicine;

  String attachment;
}
