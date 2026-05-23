package com.bookinghealth.api.dto.response.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentSlotResponse {

    LocalTime startTime;
    LocalTime endTime;
}
