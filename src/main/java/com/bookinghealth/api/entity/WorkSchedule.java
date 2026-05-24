package com.bookinghealth.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
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

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;

  @OneToMany(mappedBy = "workSchedule")
  List<AppointmentSlot> appointmentSlots;
}
