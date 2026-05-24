package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.TimeSlotTemplate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, Long> {

  boolean existsByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);

  List<TimeSlotTemplate> findByStatusOrderByStartTimeAsc(Integer status);
}
