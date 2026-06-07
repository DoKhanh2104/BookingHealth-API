package com.bookinghealth.api.dto.request.admin;

import java.time.LocalTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentSlotRequest {

  LocalTime startTime;
  LocalTime endTime;
}
