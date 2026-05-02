package com.bookinghealth.api.dto.request.admin;

import jakarta.validation.constraints.Email;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

  @Email(message = "INVALID_EMAIL")
  String email;

  String name;
  MultipartFile avatar;

  Set<String> roles;
  String status;
}
