package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.NotificationSendRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.NotificationAdminResponse;
import com.bookinghealth.api.service.NotificationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/notifications")
public class NotificationAdminController {

  NotificationService notificationService;

  @GetMapping
  public ApiResponse<Page<NotificationAdminResponse>> getNotifications(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 5, sort = "id") Pageable pageable) {
    String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    return ApiResponse.<Page<NotificationAdminResponse>>builder()
        .result(notificationService.getAllForAdmin(searchParam, pageable))
        .build();
  }

  @PostMapping
  public ApiResponse<String> sendNotification(@Valid @RequestBody NotificationSendRequest request) {
    notificationService.sendBroadcastNotification(request);
    return ApiResponse.<String>builder().result("Notification sent successfully").build();
  }

  @DeleteMapping("/{id}")
  public ApiResponse<String> deleteNotification(@PathVariable Long id) {
    notificationService.deleteNotification(id);
    return ApiResponse.<String>builder().result("Notification deleted successfully").build();
  }
}
