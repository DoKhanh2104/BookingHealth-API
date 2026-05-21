package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name = "LICH_HEN")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maLichHen", nullable = false)
  Long id;

  @Column(name = "trangThai")
  Integer status;

  @Column(name = "moTa", length = 500)
  String description;

  @Column(name = "ngayKhamDuKien")
  LocalDate expectedExaminationDate;

  @Column(name = "thoiGianThucTeDen")
  LocalDate actualArrivalTime;

  @Column(name = "tongTien")
  Double totalAmount;

  @Column(name = "phuongThuc")
  String paymentMethod;

  @Column(name = "trangThaiThanhToan")
  Integer paymentStatus;

  @Column(name = "chanDoan")
  String diagnosis;

  @Column(name = "donThuoc")
  String medicine;

  @Column(name = "tepDinhKem")
  String attachment;

  @OneToMany(mappedBy = "appointment")
  private List<DoctorReview> reviews;

  @ManyToOne
  @JoinColumn(name = "maKhungGio")
  AppointmentSlot appointmentSlot;

  @ManyToOne
  @JoinColumn(name = "maNguoiDung")
  User user;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;
}
