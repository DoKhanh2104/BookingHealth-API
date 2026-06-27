package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.response.client.NotificationResponse;
import com.bookinghealth.api.entity.Notification;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.NotificationRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

  NotificationRepository notificationRepository;
  UserRepository userRepository;

  private User getCurrentUser() {
    var context = SecurityContextHolder.getContext();
    String username = context.getAuthentication().getName();

    return userRepository
        .findByPhone(username)
        .or(() -> userRepository.findByEmail(username))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
  }

  private NotificationResponse toResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .userId(notification.getUser() != null ? notification.getUser().getId() : null)
        .title(notification.getTitle())
        .content(notification.getContent())
        .type(notification.getType())
        .isRead(notification.getStatus() != null && notification.getStatus() == 1)
        .createdAt(notification.getCreatedAt())
        .build();
  }

  public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
    User currentUser = getCurrentUser();
    Page<Notification> page =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
    return page.map(this::toResponse);
  }

  @Transactional
  public void markAsRead(Long id) {
    User currentUser = getCurrentUser();
    Notification notification =
        notificationRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND)); // Or dynamic error

    // Security check: ensure notification belongs to user
    if (notification.getUser() == null
        || !notification.getUser().getId().equals(currentUser.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    notification.setStatus(1); // 1 = Read
    notificationRepository.save(notification);
  }

  @Transactional
  public void markAllAsRead() {
    User currentUser = getCurrentUser();
    List<Notification> list = notificationRepository.findByUserId(currentUser.getId());
    for (Notification notif : list) {
      if (notif.getStatus() == null || notif.getStatus() == 0) {
        notif.setStatus(1);
      }
    }
    notificationRepository.saveAll(list);
  }

  public long getUnreadCount() {
    User currentUser = getCurrentUser();
    return notificationRepository.countByUserIdAndStatus(currentUser.getId(), 0); // 0 = Unread
  }

  @Transactional
  public void createNotification(User user, String title, String content, Integer type) {
    Notification notification =
        Notification.builder()
            .user(user)
            .title(title)
            .content(content)
            .type(type)
            .createdAt(LocalDateTime.now())
            .status(0) // Unread
            .build();
    notificationRepository.save(notification);
  }

  /** Đẩy 1 thông báo tới TẤT CẢ user có role ADMIN (vd: có đơn cần duyệt). */
  @Transactional
  public void notifyAdmins(String title, String content, Integer type) {
    List<User> admins = userRepository.findAllAdminUsers();
    if (admins.isEmpty()) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    List<Notification> notifications =
        admins.stream()
            .map(
                admin ->
                    Notification.builder()
                        .user(admin)
                        .title(title)
                        .content(content)
                        .type(type)
                        .createdAt(now)
                        .status(0) // Unread
                        .build())
            .toList();
    notificationRepository.saveAll(notifications);
  }

  // Admin methods
  public Page<com.bookinghealth.api.dto.response.admin.NotificationAdminResponse> getAllForAdmin(
      String search, Pageable pageable) {
    Page<Notification> page = notificationRepository.findAllForAdmin(search, pageable);
    return page.map(
        n ->
            com.bookinghealth.api.dto.response.admin.NotificationAdminResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .createdAt(n.getCreatedAt())
                .status(n.getStatus())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .userName(n.getUser() != null ? n.getUser().getName() : "Hệ thống")
                .userEmail(n.getUser() != null ? n.getUser().getEmail() : "")
                .build());
  }

  @Transactional
  public void sendBroadcastNotification(
      com.bookinghealth.api.dto.request.admin.NotificationSendRequest request) {
    List<User> targetUsers;
    if ("DOCTOR".equalsIgnoreCase(request.getTarget())) {
      targetUsers =
          userRepository.findAll().stream()
              .filter(u -> u.getRoles().stream().anyMatch(r -> "DOCTOR".equals(r.getRoleName())))
              .toList();
    } else if ("PATIENT".equalsIgnoreCase(request.getTarget())) {
      targetUsers =
          userRepository.findAll().stream()
              .filter(
                  u ->
                      u.getRoles().stream()
                          .noneMatch(
                              r ->
                                  "ADMIN".equals(r.getRoleName())
                                      || "DOCTOR".equals(r.getRoleName())))
              .toList();
    } else {
      // ALL
      targetUsers =
          userRepository.findAll().stream()
              .filter(u -> u.getRoles().stream().noneMatch(r -> "ADMIN".equals(r.getRoleName())))
              .toList();
    }

    LocalDateTime now = LocalDateTime.now();
    List<Notification> notifications =
        targetUsers.stream()
            .map(
                u ->
                    Notification.builder()
                        .user(u)
                        .title(request.getTitle())
                        .content(request.getContent())
                        .type(request.getType())
                        .createdAt(now)
                        .status(0)
                        .build())
            .toList();

    notificationRepository.saveAll(notifications);
  }

  @Transactional
  public void deleteNotification(Long id) {
    if (!notificationRepository.existsById(id)) {
      throw new AppException(
          ErrorCode.UNCATEGORIZED_EXCEPTION); // Could use a better code, but this works
    }
    notificationRepository.deleteById(id);
  }
}
