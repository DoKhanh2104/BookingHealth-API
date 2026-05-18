package com.bookinghealth.api.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeocodingRequest {

  @NotBlank(message = "ADDRESS_REQUIRE")
  String address;
}
