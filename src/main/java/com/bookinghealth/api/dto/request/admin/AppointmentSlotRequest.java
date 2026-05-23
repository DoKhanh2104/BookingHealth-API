package com.bookinghealth.api.dto.request.admin;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

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
