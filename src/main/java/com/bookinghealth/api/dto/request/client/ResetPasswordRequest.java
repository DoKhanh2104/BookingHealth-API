package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {

  @NotBlank(message = "Token không được để trống")
  String token;

  @NotBlank(message = "Mật khẩu mới không được để trống")
  @Size(min = 6, message = "Mật khẩu phải chứa ít nhất 6 ký tự")
  String newPassword;
}
