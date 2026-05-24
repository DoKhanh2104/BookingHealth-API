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
@Table(name = "PHONG_HOI_THOAI")
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maPhongHoiThoai", nullable = false)
  Long id;

  @ManyToOne
  @JoinColumn(name = "maNguoiDung")
  User user;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;

  @Column(name = "trangThai")
  Integer status;
}
