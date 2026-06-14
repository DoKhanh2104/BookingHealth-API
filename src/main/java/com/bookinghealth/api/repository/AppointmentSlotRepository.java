package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.AppointmentSlot;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
      @Param("workScheduleId") Long workScheduleId,
      @Param("templateStatus") Integer templateStatus);

  @Query(
      "SELECT s FROM AppointmentSlot s "
          + "JOIN FETCH s.timeSlotTemplate t "
          + "JOIN FETCH s.workSchedule w "
          + "WHERE s.id = :id")
  Optional<AppointmentSlot> findByIdWithDetails(@Param("id") Long id);

  List<AppointmentSlot> findByWorkSchedule_Id(Long workScheduleId);

  /**
   * Lấy AppointmentSlot với PESSIMISTIC WRITE lock (SELECT ... FOR UPDATE).
   * Đảm bảo chỉ 1 transaction có thể đọc+ghi slot này tại một thời điểm,
   * ngăn race condition khi 2 bệnh nhân cùng đặt cùng khung giờ.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM AppointmentSlot s WHERE s.id = :id")
  Optional<AppointmentSlot> findByIdWithLock(@Param("id") Long id);
}

