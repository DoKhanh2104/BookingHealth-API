package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {
    boolean existsByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);
}
