package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignupRequest {
  @NotBlank(message = "Số điện thoại không được để trống")
  @Size(min = 10, max = 11, message = "Số điện thoại phải từ 10 đến 11 số")
  String phone;

  @NotBlank(message = "Email không được để trống")
  @Email(message = "Email không đúng định dạng")
  String email;

  @NotBlank(message = "Mật khẩu không được để trống")
  @Size(min = 8, message = "Mật khẩu phải chứa ít nhất 8 ký tự")
  String password;

  @NotBlank(message = "Họ và tên không được để trống")
  String name;
}
