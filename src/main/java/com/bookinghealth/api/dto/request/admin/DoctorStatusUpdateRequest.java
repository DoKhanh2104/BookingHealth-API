package com.bookinghealth.api.dto.request.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorStatusUpdateRequest {

  @NotNull(message = "Status is required")
  @Min(value = 1, message = "Status must be 1 (VERIFIED) or 2 (REJECTED)")
  @Max(value = 3, message = "Status must be 1 (VERIFIED), 2 (REJECTED), or 3 (LOCKED)")
  Integer status;

  String rejectReason;
}
