package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "LICH_SU_GIA_KHAM")
public class PriceHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maLichSuGiaKham", nullable = false)
  Long id;

  @Column(name = "chiPhiKham")
  Double examinationFee;

  @Column(name = "ngayApDung")
  LocalDateTime effectiveDate;

  @Column(name = "trangThai")
  Integer status; // 1: active, 0: inactive

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;
}
