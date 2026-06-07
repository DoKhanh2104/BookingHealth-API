package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  long countByUserIdAndStatus(Long userId, Integer status);

  List<Notification> findByUserId(Long userId);

  @Query(
      value =
          "SELECT n FROM Notification n "
              + "LEFT JOIN n.user u "
              + "WHERE n.id IN (SELECT MAX(n2.id) FROM Notification n2 GROUP BY n2.title, n2.content, n2.type) "
              + "AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))) "
              + "ORDER BY n.createdAt DESC",
      countQuery =
          "SELECT count(n) FROM Notification n "
              + "WHERE n.id IN (SELECT MAX(n2.id) FROM Notification n2 GROUP BY n2.title, n2.content, n2.type) "
              + "AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<Notification> findAllForAdmin(
      @org.springframework.data.repository.query.Param("search") String search, Pageable pageable);
}
