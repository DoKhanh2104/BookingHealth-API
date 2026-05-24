package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.WorkSchedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

  List<WorkSchedule> findByDoctor_Id(Long doctorId);

  @Query(
      "SELECT w FROM WorkSchedule w "
          + "JOIN FETCH w.doctor d "
          + "WHERE d.id = :doctorId AND w.workDate = :workDate")
  Optional<WorkSchedule> findByDoctorIdAndWorkDate(
      @Param("doctorId") Long doctorId, @Param("workDate") LocalDate workDate);
}
