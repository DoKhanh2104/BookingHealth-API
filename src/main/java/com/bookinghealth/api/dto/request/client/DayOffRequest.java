package com.bookinghealth.api.dto.request.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DayOffRequest {
  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate startDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate endDate;

  String reason;
}
