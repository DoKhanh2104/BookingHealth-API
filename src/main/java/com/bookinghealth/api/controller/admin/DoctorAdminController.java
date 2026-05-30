package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.DoctorAdminResponse;
import com.bookinghealth.api.service.DoctorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import com.bookinghealth.api.dto.request.admin.DoctorStatusUpdateRequest;

@RestController
@RequestMapping("/admin/doctors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorAdminController {

  DoctorService doctorService;

  @GetMapping
  public ApiResponse<Page<DoctorAdminResponse>> getDoctors(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Integer status) {
    return ApiResponse.<Page<DoctorAdminResponse>>builder()
        .result(doctorService.getAllDoctors(page, size, search, status))
        .build();
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<String> updateDoctorStatus(
      @PathVariable Long id, 
      @Valid @RequestBody DoctorStatusUpdateRequest request) {
    doctorService.updateDoctorStatus(id, request);
    return ApiResponse.<String>builder()
        .result("Cập nhật trạng thái bác sĩ thành công")
        .build();
  }

  @GetMapping("/work-schedules")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<com.bookinghealth.api.dto.response.admin.WorkScheduleAdminResponse>> getWorkSchedules(
      @RequestParam String date,
      @RequestParam(required = false) Long clinicId,
      @RequestParam(required = false) Long doctorId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.<Page<com.bookinghealth.api.dto.response.admin.WorkScheduleAdminResponse>>builder()
        .result(doctorService.getWorkSchedulesForAdmin(date, clinicId, doctorId, org.springframework.data.domain.PageRequest.of(page, size)))
        .build();
  }
}
