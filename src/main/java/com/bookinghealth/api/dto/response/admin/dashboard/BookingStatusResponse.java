package com.bookinghealth.api.dto.response.admin.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingStatusResponse {

  long pending;
  long completed;
  long cancelled;
}
