package com.bookinghealth.api.dto.response.client;

import java.time.LocalDate;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleResponse {

  Long id;
  Long doctorId;
  LocalDate workDate;
  List<ScheduleSlotResponse> slots;
}
