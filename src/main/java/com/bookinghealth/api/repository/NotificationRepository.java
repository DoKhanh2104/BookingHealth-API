package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  long countByUserIdAndStatus(Long userId, Integer status);

  List<Notification> findByUserId(Long userId);

  @Query("SELECT n FROM Notification n " +
         "LEFT JOIN n.user u " +
         "WHERE (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))) " +
         "ORDER BY n.createdAt DESC")
  Page<Notification> findAllForAdmin(@org.springframework.data.repository.query.Param("search") String search, Pageable pageable);
}
