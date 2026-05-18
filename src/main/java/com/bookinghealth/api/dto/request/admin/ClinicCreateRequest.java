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
public class ClinicCreateRequest {

  @NotBlank(message = "Clinic name cannot be blank")
  String name;

  @NotBlank(message = "Address cannot be blank")
  String address;

  @NotNull(message = "Longitude cannot be null")
  Double longitude;

  @NotNull(message = "Latitude cannot be null")
  Double latitude;
}
