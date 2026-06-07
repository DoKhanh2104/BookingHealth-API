package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.DoctorReview;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {

  boolean existsByAppointmentId(Long appointmentId);

  Page<DoctorReview> findByDoctorId(Long doctorId, Pageable pageable);

  List<DoctorReview> findByDoctorId(Long doctorId);

  // ─────────────────────────────────────────────────────────────────────
  // Dashboard queries
  // ─────────────────────────────────────────────────────────────────────

  /**
   * Lấy feedback có rating thấp (≤ maxRating) sắp xếp theo ID giảm dần (mới nhất trước). Kèm thông
   * tin bác sĩ và bệnh nhân.
   */
  @Query(
      "SELECT r FROM DoctorReview r JOIN FETCH r.doctor d JOIN FETCH d.user du JOIN FETCH r.user u WHERE r.rating <= :maxRating ORDER BY r.id DESC")
  List<DoctorReview> findRecentLowRatingFeedbacks(
      @Param("maxRating") int maxRating, Pageable pageable);

  /** Tính điểm trung bình của một bác sĩ. */
  @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM DoctorReview r WHERE r.doctor.id = :doctorId")
  double avgRatingByDoctorId(@Param("doctorId") Long doctorId);

  @Query(
      "SELECT r.doctor.id AS doctorId, "
          + "r.doctor.user.name AS doctorName, "
          + "MIN(s.specialtyName) AS specialtyName, "
          + "COUNT(r.id) AS totalReviews, "
          + "COALESCE(AVG(r.rating), 0.0) AS averageRating, "
          + "SUM(CASE WHEN r.rating <= 2 THEN 1 ELSE 0 END) AS negativeReviews "
          + "FROM DoctorReview r "
          + "LEFT JOIN r.doctor.specialties s "
          + "WHERE (cast(:fromDate as date) IS NULL OR r.appointment.expectedExaminationDate >= :fromDate) "
          + "AND (cast(:toDate as date) IS NULL OR r.appointment.expectedExaminationDate <= :toDate) "
          + "GROUP BY r.doctor.id, r.doctor.user.name")
  List<com.bookinghealth.api.dto.projection.SatisfactionReportProjection> getSatisfactionReport(
      @Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate);
}
