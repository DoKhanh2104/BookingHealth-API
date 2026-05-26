package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {
  boolean existsByAppointmentId(Long appointmentId);

  org.springframework.data.domain.Page<DoctorReview> findByDoctorId(Long doctorId, org.springframework.data.domain.Pageable pageable);

  java.util.List<DoctorReview> findByDoctorId(Long doctorId);
}
