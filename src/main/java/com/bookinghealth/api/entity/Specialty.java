package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
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
@Table(name = "CHUYEN_KHOA")
public class Specialty {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maChuyenKhoa", nullable = false)
  Long id;

  @Column(name = "tenChuyenKhoa", length = 50)
  String specialtyName;

  @Column(name = "mota")
  String description;

  @ManyToMany(mappedBy = "specialties")
  Set<Doctor> doctors = new LinkedHashSet<>();
}
