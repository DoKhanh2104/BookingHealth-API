package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.AppointmentSlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

  boolean existsByWorkScheduleIdAndTimeSlotTemplateId(Long workScheduleId, Long templateId);

  @Query(
      "SELECT s FROM AppointmentSlot s "
          + "JOIN FETCH s.timeSlotTemplate t "
          + "WHERE s.workSchedule.id = :workScheduleId "
          + "AND t.status = :templateStatus "
          + "ORDER BY t.startTime ASC")
  List<AppointmentSlot> findByWorkScheduleIdWithActiveTemplates(
      @Param("workScheduleId") Long workScheduleId, @Param("templateStatus") Integer templateStatus);

  @Query(
      "SELECT s FROM AppointmentSlot s "
          + "JOIN FETCH s.timeSlotTemplate t "
          + "JOIN FETCH s.workSchedule w "
          + "WHERE s.id = :id")
  java.util.Optional<AppointmentSlot> findByIdWithDetails(@Param("id") Long id);
}
