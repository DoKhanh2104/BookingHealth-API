package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);

  Optional<User> findByPhone(String phone);

  Optional<User> findByEmail(String email);

  /** Tất cả user có role ADMIN — dùng để đẩy thông báo tới admin. */
  @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.roleName = 'ADMIN'")
  List<User> findAllAdminUsers();

  @Query(
      value =
          "SELECT u FROM User u JOIN u.roles r "
              + "WHERE r.roleName = 'USER' "
              + "AND u.status != 0 "
              + "AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.roleName IN ('DOCTOR', 'ADMIN')) "
              + "AND (:search IS NULL OR u.name LIKE %:search% OR u.phone LIKE %:search% OR u.email LIKE %:search%)",
      countQuery =
          "SELECT count(u) FROM User u JOIN u.roles r "
              + "WHERE r.roleName = 'USER' "
              + "AND u.status != 0 "
              + "AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.roleName IN ('DOCTOR', 'ADMIN')) "
              + "AND (:search IS NULL OR u.name LIKE %:search% OR u.phone LIKE %:search% OR u.email LIKE %:search%)")
  Page<User> findPatientsForAdmin(@Param("search") String search, Pageable pageable);

  // ─────────────────────────────────────────────────────────────────────
  // Dashboard queries
  // ─────────────────────────────────────────────────────────────────────

  /** Đếm tổng số user có role 'USER' (bệnh nhân) — không bao gồm ADMIN/DOCTOR. */
  @Query(
      "SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.roleName = 'USER' AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.roleName IN ('DOCTOR', 'ADMIN'))")
  long countAllPatients();

  /**
   * Đếm user mới đăng ký trong khoảng ngày. Vì User không có createdAt, dùng ID thay thế tạm — hoặc
   * nếu có thể, thêm createdAt vào entity. Hiện tại: đếm tất cả user có role USER để tính tổng.
   */
  @Query(
      "SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.roleName = 'USER' AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.roleName IN ('DOCTOR', 'ADMIN')) AND u.id > :sinceId")
  long countNewPatientsSinceId(@Param("sinceId") Long sinceId);
}
