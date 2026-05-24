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
@Table(name = "THONG_BAO")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maThongBao", nullable = false)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "maNguoiDung")
  User user;

  @Column(name = "tieuDe")
  String title;

  @Column(name = "noiDung")
  String content;

  @Column(name = "loai")
  Integer type; // 1=Appointment, 2=System, 3=Reminder

  @Column(name = "den")
  LocalDateTime createdAt;

  @Column(name = "trangThai")
  Integer status; // 0=Unread, 1=Read
}
