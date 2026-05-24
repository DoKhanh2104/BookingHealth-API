package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.client.NotificationResponse;
import com.bookinghealth.api.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

  NotificationService notificationService;

  @GetMapping("/me")
  public ApiResponse<Page<NotificationResponse>> getMyNotifications(
      @PageableDefault(size = 20) Pageable pageable) {
    return ApiResponse.<Page<NotificationResponse>>builder()
        .result(notificationService.getMyNotifications(pageable))
        .build();
  }

  @PutMapping("/{id}/read")
  public ApiResponse<Void> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return ApiResponse.<Void>builder().build();
  }

  @PutMapping("/read-all")
  public ApiResponse<Void> markAllAsRead() {
    notificationService.markAllAsRead();
    return ApiResponse.<Void>builder().build();
  }

  @GetMapping("/me/unread-count")
  public ApiResponse<Long> getUnreadCount() {
    return ApiResponse.<Long>builder()
        .result(notificationService.getUnreadCount())
        .build();
  }
}
