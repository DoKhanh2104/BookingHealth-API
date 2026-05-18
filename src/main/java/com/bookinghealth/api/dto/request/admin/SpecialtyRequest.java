package com.bookinghealth.api.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecialtyRequest {
  @NotBlank(message = "SPECIALTY_NAME_REQUIRE")
  String specialtyName;

  String description;
}
