package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorSignupRequest {
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

  @NotBlank(message = "Số giấy phép hành nghề không được để trống")
  String practiceLicenseNumber;

  @NotNull(message = "Ngày bắt đầu hành nghề không được để trống")
  @org.springframework.format.annotation.DateTimeFormat(
      iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  LocalDate practiceStartDate;

  String biography;

  org.springframework.web.multipart.MultipartFile avatar;

  org.springframework.web.multipart.MultipartFile practiceLicenseImage;

  Long clinicId;

  java.util.List<Long> specialtyIds;
}
