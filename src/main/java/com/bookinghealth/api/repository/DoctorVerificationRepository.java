package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorVerificationRepository extends JpaRepository<DoctorVerification, Long> {
  Optional<DoctorVerification> findByDoctor(Doctor doctor);
}
