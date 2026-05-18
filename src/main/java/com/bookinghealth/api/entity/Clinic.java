package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "PHONG_KHAM")
public class Clinic {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maPhongKham", nullable = false)
  Long id;

  @Column(name = "tenPhongKham", length = 255)
  String clinicName;

  @Column(name = "diaChi", length = 250)
  String address;

  @Column(name = "kinhDo")
  Double longitude;

  @Column(name = "viDo")
  Double latitude;

  @OneToMany(mappedBy = "clinic", fetch = FetchType.LAZY)
  List<Doctor> doctors;
}
