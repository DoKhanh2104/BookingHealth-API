package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

  @NotNull(message = "Mã bác sĩ không được để trống")
  Long doctorId;

  @NotNull(message = "Mã lịch hẹn không được để trống")
  Long appointmentId;

  @NotNull(message = "Số sao đánh giá không được để trống")
  @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
  @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
  Integer rating;

  String comment;
}
