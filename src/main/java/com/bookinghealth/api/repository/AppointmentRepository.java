package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  boolean existsByAppointmentSlotIdAndStatusNotIn(Long appointmentSlotId, java.util.List<Integer> statuses);

  org.springframework.data.domain.Page<Appointment> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

  org.springframework.data.domain.Page<Appointment> findByUserIdAndStatus(Long userId, Integer status, org.springframework.data.domain.Pageable pageable);

  org.springframework.data.domain.Page<Appointment> findByDoctorId(Long doctorId, org.springframework.data.domain.Pageable pageable);

  org.springframework.data.domain.Page<Appointment> findByDoctorIdAndStatus(Long doctorId, Integer status, org.springframework.data.domain.Pageable pageable);

  java.util.List<Appointment> findByDoctorId(Long doctorId);

  java.util.List<Appointment> findByDoctorIdAndExpectedExaminationDate(Long doctorId, java.time.LocalDate date);
}
