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
@Table(name = "MA_DAT_LAI_MAT_KHAU")
public class PasswordReset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maDatLaiMatKhau", nullable = false)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "maNguoiDung", referencedColumnName = "maNguoiDung")
  private User user;

  @Column(name = "token", nullable = false, unique = true)
  private String token;

  @Column(name = "thoiGianHetHan", nullable = false)
  private LocalDateTime expirationTime;
}
