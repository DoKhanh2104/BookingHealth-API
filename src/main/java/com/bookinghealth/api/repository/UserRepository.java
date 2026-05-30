package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);

  // Return true if there is another user (not the given id) with the given email
  boolean existsByEmailAndIdNot(String email, Long id);

  Optional<User> findByPhone(String phone);

  Optional<User> findByEmail(String email);

  @Query(
      value = "SELECT u FROM User u JOIN u.roles r "
          + "WHERE r.roleName = 'USER' "
          + "AND u.status != 0 "
          + "AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.roleName IN ('DOCTOR', 'ADMIN')) "
          + "AND (:search IS NULL OR u.name LIKE %:search% OR u.phone LIKE %:search% OR u.email LIKE %:search%)",
      countQuery = "SELECT count(u) FROM User u JOIN u.roles r "
          + "WHERE r.roleName = 'USER' "
          + "AND u.status != 0 "
          + "AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.roleName IN ('DOCTOR', 'ADMIN')) "
          + "AND (:search IS NULL OR u.name LIKE %:search% OR u.phone LIKE %:search% OR u.email LIKE %:search%)"
  )
  Page<User> findPatientsForAdmin(
      @Param("search") String search,
      Pageable pageable);
}
