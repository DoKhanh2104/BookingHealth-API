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
@Table(name = "YEU_CAU_XAC_THUC")
public class DoctorVerification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maYeuCauXacThuc", nullable = false)
  Long id;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;

  @ManyToOne
  @JoinColumn(name = "maQuanTriVien")
  User admin;

  @Column(name = "trangThai")
  Integer status;

  @Column(name = "lyDoTuChoi", length = 255)
  String reason;
}
