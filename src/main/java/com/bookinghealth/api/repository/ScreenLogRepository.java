package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.ScreenLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenLogRepository extends JpaRepository<ScreenLog, Long> {
}
