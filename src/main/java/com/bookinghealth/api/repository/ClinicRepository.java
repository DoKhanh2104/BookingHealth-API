package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
  boolean existsByClinicNameAndAddress(String clinicName, String address);
}
