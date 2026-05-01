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
@Table(name = "Nguoi_Dung")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maNguoiDung")
    Long id;

    @Column(name = "soDienThoai")
    String phone;

    @Column(name = "email")
    String email;

    @Column(name = "matKhau")
    String password;

    @Column(name = "hoTen")
    String name;

    @Column(name = "trangThai")
    Integer status;

    @Column(name = "anhDaiDien")
    String avatar;
}
