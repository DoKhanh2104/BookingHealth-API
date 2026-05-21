package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "KHUNG_GIO")
public class AppointmentSlot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maKhungGio", nullable = false)
  Long id;

  @Column(name = "thoiGianBatDau")
  LocalTime startTime;

  @Column(name = "thoiGianKetThuc")
  LocalTime endTime;

  @Column(name = "trangThai")
  Integer status;

  @ManyToOne
  @JoinColumn(name = "maLichLamViec")
  WorkSchedule workSchedule;

  @OneToMany(mappedBy = "appointmentSlot")
  List<Appointment> appointments;
}
