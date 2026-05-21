package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "DANH_GIA_BAC_SI")
public class DoctorReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maDanhGiaBacSi", nullable = false)
  Long id;

  @Column(name = "danhGia")
  Integer rating;

  @Column(name = "binhLuan", length = 500)
  String comment;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;

  @ManyToOne
  @JoinColumn(name = "maNguoiDung")
  User user;

  @ManyToOne
  @JoinColumn(name = "maLichHen")
  Appointment appointment;
}
