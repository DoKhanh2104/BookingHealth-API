package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordRequest {

  @NotBlank(message = "EMAIL_REQUIRE")
  @Email(message = "INVALID_EMAIL")
  String email;
}
