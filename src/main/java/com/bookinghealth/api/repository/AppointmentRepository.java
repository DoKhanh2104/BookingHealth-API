package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  boolean existsByAppointmentSlotIdAndStatusNotIn(Long appointmentSlotId, java.util.List<Integer> statuses);
}
