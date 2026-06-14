package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Qualification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QualificationRepository extends JpaRepository<Qualification, Long> {
  List<Qualification> findByDoctorId(Long doctorId);
  Page<Qualification> findByStatus(Integer status, Pageable pageable);
}
