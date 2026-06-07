package com.bookinghealth.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Ca khám của bác sĩ theo ngày (junction): lịch làm việc + khung giờ chuẩn. trangThai: bác sĩ mở
 * (1) / đóng (0) ca đó.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "KHUNG_GIO",
    uniqueConstraints = @UniqueConstraint(columnNames = {"maLichLamViec", "maCaKham"}))
public class AppointmentSlot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maKhungGio", nullable = false)
  Long id;

  /** Bác sĩ mở/đóng ca */
  @Column(name = "trangThai")
  Integer status;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "maLichLamViec", nullable = false)
  WorkSchedule workSchedule;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "maCaKham", nullable = false)
  TimeSlotTemplate timeSlotTemplate;

  @JsonIgnore
  @OneToMany(mappedBy = "appointmentSlot")
  List<Appointment> appointments;
}
