package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "LICH_LAM_VIEC")
public class WorkSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maLichLamViec", nullable = false)
  Long id;

  @Column(name = "ngayLamViec")
  LocalDate workDate;

  @Column(name = "trangThai")
  Integer status;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;

  @OneToMany(mappedBy = "workSchedule")
  Set<AppointmentSlot> appointmentSlots;
}
