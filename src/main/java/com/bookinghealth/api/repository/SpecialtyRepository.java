package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    boolean existsBySpecialtyName(String name);
}

