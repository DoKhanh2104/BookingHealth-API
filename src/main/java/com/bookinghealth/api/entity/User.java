package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.List;
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
@Table(name = "NGUOI_DUNG")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maNguoiDung", nullable = false)
  Long id;

  @Column(name = "soDienThoai", length = 20)
  String phone;

  @Column(name = "email", length = 50)
  String email;

  @Column(name = "matKhau")
  String password;

  @Column(name = "hoTen", length = 50)
  String name;

  @Column(name = "trangThai")
  Integer status;

  @Column(name = "anhDaiDien")
  String avatar;

  /**
   * Số lần vi phạm hủy lịch trong 48h. Khi đạt 3 lần → isBlacklisted = true.
   */
  @Column(name = "soLanViPham", nullable = false, columnDefinition = "INT DEFAULT 0")
  Integer penaltyCount = 0;

  /**
   * Đánh dấu tài khoản bị khóa đặt lịch do vi phạm hủy nhiều lần.
   * 0 = bình thường, 1 = bị chặn đặt lịch.
   */
  @Column(name = "biChanDatLich", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
  Integer isBlacklisted = 0;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "VAI_TRO_NGUOI_DUNG",
      joinColumns = @JoinColumn(name = "maNguoiDung"),
      inverseJoinColumns = @JoinColumn(name = "maVaiTro"))
  Set<Role> roles = new LinkedHashSet<>();

  @OneToOne(mappedBy = "user")
  private Doctor doctor;

  @OneToMany(mappedBy = "user")
  private List<DoctorReview> reviews;

  @OneToMany(mappedBy = "user")
  private List<Appointment> appointments;
}
