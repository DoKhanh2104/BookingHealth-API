package com.bookinghealth.api.dto.response;

import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

  Long id;
  String phone;
  String email;
  String name;
  String avatar;
  int status;
  Set<RoleResponse> roles;
}
