package com.bookinghealth.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

/** Khung giờ chuẩn do Admin cấu hình (~15 bản ghi). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "CA_KHAM_CHUAN")
public class TimeSlotTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maCaKham", nullable = false)
  Long id;

  @JsonFormat(pattern = "HH:mm")
  @Column(name = "thoiGianBatDau")
  LocalTime startTime;

  @JsonFormat(pattern = "HH:mm")
  @Column(name = "thoiGianKetThuc")
  LocalTime endTime;

  /** 1: admin bật (hiển thị cho bác sĩ), 0: admin tắt (ẩn khỏi bác sĩ) */
  @Column(name = "trangThai")
  Integer status;
}
