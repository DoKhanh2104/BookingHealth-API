package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.DayOffRejectRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.DayOffResponse;
import com.bookinghealth.api.service.DoctorDayOffService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/day-offs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class DayOffAdminController {

  DoctorDayOffService doctorDayOffService;

  @GetMapping
  public ApiResponse<Page<DayOffResponse>> getAllDayOffs(
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.<Page<DayOffResponse>>builder()
        .result(doctorDayOffService.getAllDayOffs(status, PageRequest.of(page, size)))
        .build();
  }

  @PutMapping("/{id}/approve")
  public ApiResponse<DayOffResponse> approveDayOff(@PathVariable Long id) {
    return ApiResponse.<DayOffResponse>builder()
        .result(doctorDayOffService.approveDayOff(id))
        .build();
  }

  @PutMapping("/{id}/reject")
  public ApiResponse<DayOffResponse> rejectDayOff(
      @PathVariable Long id, @RequestBody(required = false) DayOffRejectRequest request) {
    String reason = request != null ? request.getReason() : null;
    return ApiResponse.<DayOffResponse>builder()
        .result(doctorDayOffService.rejectDayOff(id, reason))
        .build();
  }
}
