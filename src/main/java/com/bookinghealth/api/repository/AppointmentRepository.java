package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Appointment;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  boolean existsByAppointmentSlotIdAndStatusNotIn(Long appointmentSlotId, List<Integer> statuses);

  Page<Appointment> findByUserId(Long userId, Pageable pageable);

  Page<Appointment> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

  Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

  Page<Appointment> findByDoctorIdAndStatus(Long doctorId, Integer status, Pageable pageable);

  List<Appointment> findByDoctorId(Long doctorId);

  List<Appointment> findByDoctorIdAndExpectedExaminationDate(Long doctorId, LocalDate date);

  // ─────────────────────────────────────────────────────────────────────
  // Dashboard queries
  // ─────────────────────────────────────────────────────────────────────

  /** Đếm tổng số booking trong khoảng ngày */
  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.expectedExaminationDate BETWEEN :from AND :to")
  long countByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

  /** Đếm booking theo status trong khoảng ngày */
  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.expectedExaminationDate BETWEEN :from AND :to AND a.status = :status")
  long countByDateRangeAndStatus(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("status") int status);

  long countByStatus(int status);

  /** Tổng doanh thu trong khoảng ngày (chỉ tính booking đã hoàn thành - status=2) */
  @Query("SELECT COALESCE(SUM(a.totalAmount), 0) FROM Appointment a WHERE a.expectedExaminationDate BETWEEN :from AND :to AND a.status = 2")
  double sumRevenueByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

  /** Doanh thu theo từng ngày trong khoảng (dùng cho revenue chart - weekly) */
  @Query("SELECT a.expectedExaminationDate, COALESCE(SUM(a.totalAmount), 0) FROM Appointment a WHERE a.expectedExaminationDate BETWEEN :from AND :to AND a.status = 2 GROUP BY a.expectedExaminationDate ORDER BY a.expectedExaminationDate")
  List<Object[]> revenueGroupByDay(@Param("from") LocalDate from, @Param("to") LocalDate to);

  /** Doanh thu theo năm-tháng (YYYY-MM) */
  @Query(value = "SELECT DATE_FORMAT(a.ngayKhamDuKien, '%Y-%m') as ym, COALESCE(SUM(a.tongTien), 0) FROM LICH_HEN a WHERE a.ngayKhamDuKien BETWEEN :from AND :to AND a.trangThai = 2 GROUP BY ym ORDER BY ym", nativeQuery = true)
  List<Object[]> revenueGroupByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

  /** Top bác sĩ theo số lượt đặt hàng thành công trong khoảng ngày */
  @Query("SELECT a.doctor.id, COUNT(a) FROM Appointment a WHERE a.expectedExaminationDate BETWEEN :from AND :to AND a.status = 2 GROUP BY a.doctor.id ORDER BY COUNT(a) DESC")
  List<Object[]> topDoctorsByBookings(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

  @Query("SELECT a.doctor.id, COUNT(a) FROM Appointment a WHERE a.status = 2 GROUP BY a.doctor.id ORDER BY COUNT(a) DESC")
  List<Object[]> topDoctorsByBookingsAllTime(Pageable pageable);

  /** Phân bố booking theo chuyên khoa trong khoảng ngày */
  @Query("SELECT s.specialtyName, COUNT(a) FROM Appointment a JOIN a.doctor d JOIN d.specialties s WHERE a.expectedExaminationDate BETWEEN :from AND :to GROUP BY s.specialtyName ORDER BY COUNT(a) DESC")
  List<Object[]> bookingsBySpecialty(@Param("from") LocalDate from, @Param("to") LocalDate to);

  @Query("SELECT s.specialtyName, COUNT(a) FROM Appointment a JOIN a.doctor d JOIN d.specialties s GROUP BY s.specialtyName ORDER BY COUNT(a) DESC")
  List<Object[]> bookingsBySpecialtyAllTime();

  @Query("SELECT a.id AS id, " +
         "a.user.name AS patientName, " +
         "a.doctor.user.name AS doctorName, " +
         "a.totalAmount AS amount, " +
         "'Tiền mặt' AS paymentMethod, " +
         "a.expectedExaminationDate AS paymentTime " +
         "FROM Appointment a " +
         "LEFT JOIN a.doctor.specialties s " +
         "WHERE a.status = 2 " +
         "AND (:specialtyId IS NULL OR s.id = :specialtyId) " +
         "AND (:clinicId IS NULL OR a.doctor.clinic.id = :clinicId) " +
         "AND (cast(:fromDate as date) IS NULL OR a.expectedExaminationDate >= :fromDate) " +
         "AND (cast(:toDate as date) IS NULL OR a.expectedExaminationDate <= :toDate) " +
         "ORDER BY a.expectedExaminationDate DESC")
  List<Object[]> getFinancialReport(
      @Param("fromDate") java.time.LocalDate fromDate,
      @Param("toDate") java.time.LocalDate toDate,
      @Param("specialtyId") Long specialtyId,
      @Param("clinicId") Long clinicId);

  @Query("SELECT d.id AS doctorId, " +
         "d.user.name AS doctorName, " +
         "COUNT(a.id) AS total, " +
         "SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) AS completed, " +
         "SUM(CASE WHEN a.status = 3 THEN 1 ELSE 0 END) AS cancelled " +
         "FROM Appointment a " +
         "JOIN a.doctor d " +
         "LEFT JOIN d.specialties s " +
         "WHERE (:specialtyId IS NULL OR s.id = :specialtyId) " +
         "AND (:clinicId IS NULL OR d.clinic.id = :clinicId) " +
         "AND (cast(:fromDate as date) IS NULL OR a.expectedExaminationDate >= :fromDate) " +
         "AND (cast(:toDate as date) IS NULL OR a.expectedExaminationDate <= :toDate) " +
         "GROUP BY d.id, d.user.name")
  List<com.bookinghealth.api.dto.projection.PerformanceReportProjection> getPerformanceReport(
      @Param("fromDate") java.time.LocalDate fromDate,
      @Param("toDate") java.time.LocalDate toDate,
      @Param("specialtyId") Long specialtyId,
      @Param("clinicId") Long clinicId);
}
