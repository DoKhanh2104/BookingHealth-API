package com.bookinghealth.api.repository;

import com.bookinghealth.api.dto.response.admin.DoctorAdminResponse;
import com.bookinghealth.api.entity.Doctor;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

  java.util.Optional<Doctor> findByUser_Id(Long userId);

  @Query(
      "SELECT new com.bookinghealth.api.dto.response.admin.DoctorAdminResponse("
          + "d.id, u.name, u.email, u.phone, c.clinicName, d.practiceLicenseNumber, d.status, s.specialtyName, d.practiceStartDate, u.avatar) "
          + "FROM Doctor d "
          + "JOIN d.user u "
          + "LEFT JOIN d.clinic c "
          + "LEFT JOIN d.specialties s "
          + "WHERE (:search IS NULL OR u.name LIKE %:search% OR d.practiceLicenseNumber LIKE %:search%) "
          + "AND (:status IS NULL OR d.status = :status)")
  Page<DoctorAdminResponse> searchDoctorsForAdmin(
      @Param("search") String search, @Param("status") Integer status, Pageable pageable);

  boolean existsByPracticeLicenseNumber(String practiceLicenseNumber);

  @Query(
      "SELECT DISTINCT d FROM Doctor d "
          + "JOIN d.user u "
          + "LEFT JOIN d.specialties s "
          + "WHERE d.status = 1 "
          + "AND (:specialtyId IS NULL OR s.id = :specialtyId) "
          + "AND (:clinicId IS NULL OR d.clinic.id = :clinicId) "
          + "AND (:search IS NULL OR u.name LIKE %:search%)")
  Page<Doctor> searchDoctorsForClient(
      @Param("specialtyId") Long specialtyId,
      @Param("clinicId") Long clinicId,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      value = "SELECT d FROM Doctor d JOIN FETCH d.user u LEFT JOIN FETCH d.clinic c "
          + "WHERE d.status = 1 "
          + "AND (:clinicId IS NULL OR c.id = :clinicId) "
          + "AND (:doctorId IS NULL OR d.id = :doctorId)",
      countQuery = "SELECT count(d) FROM Doctor d WHERE d.status = 1 "
          + "AND (:clinicId IS NULL OR d.clinic.id = :clinicId) "
          + "AND (:doctorId IS NULL OR d.id = :doctorId)"
  )
  Page<Doctor> findActiveDoctorsForSchedule(
      @Param("clinicId") Long clinicId,
      @Param("doctorId") Long doctorId,
      Pageable pageable);

  // ─────────────────────────────────────────────────────────────────────
  // Dashboard queries
  // ─────────────────────────────────────────────────────────────────────

  /**
   * Đếm bác sĩ PENDING (status = 0) chưa được duyệt.
   */
  long countByStatus(Integer status);

  /**
   * Đếm bác sĩ mới đăng ký (practiceStartDate trong khoảng).
   * status = 0 (PENDING) hoặc 1 (APPROVED) — lấy tất cả để tính "bác sĩ mới trong kỳ".
   */
  @Query("SELECT COUNT(d) FROM Doctor d WHERE d.practiceStartDate BETWEEN :from AND :to")
  long countNewDoctorsByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

  /**
   * Bác sĩ đang chờ phê duyệt (status = 0), lấy kèm thông tin user và specialty.
   */
  @Query("SELECT d FROM Doctor d JOIN FETCH d.user u LEFT JOIN FETCH d.specialties s WHERE d.status = 0 ORDER BY d.practiceStartDate DESC")
  List<Doctor> findPendingDoctors();
}
