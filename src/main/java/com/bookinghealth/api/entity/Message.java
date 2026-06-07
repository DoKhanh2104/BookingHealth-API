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
@Table(name = "TIN_NHAN")
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maTinNhan", nullable = false)
  Long id;

  @ManyToOne
  @JoinColumn(name = "maPhongHoiThoai")
  ChatRoom chatRoom;

  @ManyToOne
  @JoinColumn(name = "maNguoiDung")
  User sender;

  @Column(name = "noiDung", length = 500)
  String content;

  @Column(name = "thoiGianGui")
  LocalDateTime sendTime;
}
