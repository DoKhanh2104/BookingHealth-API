package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name = "BAC_SI")
public class Doctor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maBacSi", nullable = false)
  Long id;

  @Column(name = "tieuSu", length = 500)
  String biography;

  @Column(name = "ngayBatDauHanhNghe")
  LocalDate practiceStartDate;

  @Column(name = "soGPHN")
  String practiceLicenseNumber;

  @Column(name = "anhChungChi")
  String practiceLicenseImage;

  @Column(name = "trangThaiXacNhan")
  Integer status;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "BAC_SI_CHUYEN_KHOA",
      joinColumns = @JoinColumn(name = "maBacSi"),
      inverseJoinColumns = @JoinColumn(name = "maChuyenKhoa"))
  Set<Specialty> specialties = new LinkedHashSet<>();

  @OneToOne
  @JoinColumn(name = "maNguoiDung", referencedColumnName = "maNguoiDung", unique = true)
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "maPhongKham")
  Clinic clinic;

  @OneToMany(mappedBy = "doctor")
  List<DoctorReview> reviews;

  @OneToMany(mappedBy = "doctor")
  List<Qualification> qualifications;

  @OneToMany(mappedBy = "doctor")
  List<PriceHistory> priceHistories;

  @OneToMany(mappedBy = "doctor")
  List<WorkSchedule> workSchedules;

  @OneToMany(mappedBy = "doctor")
  List<Appointment> appointments;
}
