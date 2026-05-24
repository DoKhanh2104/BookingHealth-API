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
@Table(name = "NHAT_KY_SANG_LOC")
public class ScreenLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maBanGhi", nullable = false)
  Long id;

  @ManyToOne
  @JoinColumn(name = "maNguoiDung")
  User user;

  @ManyToOne
  @JoinColumn(name = "maChuyenKhoaDeXuat")
  Specialty suggestedSpecialty;

  @Column(name = "trieuChung", length = 255)
  String symptoms;
}
