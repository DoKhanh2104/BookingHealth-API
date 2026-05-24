package com.bookinghealth.api.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentSlotResponse {

  Long id;

  @JsonFormat(pattern = "HH:mm")
  LocalTime startTime;

  @JsonFormat(pattern = "HH:mm")
  LocalTime endTime;

  Integer status;
}
