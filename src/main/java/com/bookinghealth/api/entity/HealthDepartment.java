package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "SO_Y_TE")
public class HealthDepartment {

  @Id
  @Column(name = "soGPHD", nullable = false)
  String id;

  @Column(name = "ngayCap")
  LocalDate issuedDate;

  @Column(name = "loaiHinh")
  String facilityType;

  @Column(name = "hinhThucToChuc")
  String organizationType;

  @Column(name = "coSoKhamChuaBenh")
  String medicalFacilityName;

  @Column(name = "diaChi")
  String address;

  @Column(name = "quanHuyen")
  String district;

  @Column(name = "xaPhuong")
  String ward;

  @Column(name = "soDienThoai")
  String phoneNumber;

  @Column(name = "tenBacSi")
  String doctorName;
}
