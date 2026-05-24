package com.bookinghealth.api.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "NGAY_NGHI_PHEP")
public class DoctorDayOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maNgayNghi", nullable = false)
    Long id;

    @Column(name = "ngayBatDau")
    LocalDate startDate;

    @Column(name = "ngayKetThuc")
    LocalDate endDate;

    @Column(name = "lyDo")
    String reason;

    @Column(name = "trangThai")
    Integer status;

    @ManyToOne
    @JoinColumn(name = "maBacSi")
    Doctor doctor;
}
