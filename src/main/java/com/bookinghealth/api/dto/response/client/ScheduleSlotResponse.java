package com.bookinghealth.api.dto.response.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleSlotResponse {

  /** ID bản ghi KHUNG_GIO (dùng đặt lịch / toggle bác sĩ) */
  Long id;

  Long templateId;

  @JsonFormat(pattern = "HH:mm")
  LocalTime startTime;

  @JsonFormat(pattern = "HH:mm")
  LocalTime endTime;

  Long workScheduleId;

  /** Bác sĩ đang mở ca (1) hay đóng (0) */
  boolean doctorOpen;

  /** Đã có lịch hẹn chưa huỷ */
  boolean booked;

  /** Có thể đặt lịch (bác sĩ mở + chưa đặt + admin bật ca) */
  boolean available;
}
