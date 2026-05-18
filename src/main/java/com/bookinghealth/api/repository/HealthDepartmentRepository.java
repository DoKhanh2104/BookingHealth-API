package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.HealthDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthDepartmentRepository extends JpaRepository<HealthDepartment, String> {}
