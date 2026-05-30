package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/patients")
@PreAuthorize("hasRole('ADMIN')")
public class PatientAdminController {

  UserService userService;
  com.bookinghealth.api.service.AppointmentService appointmentService;

  @GetMapping
  public ApiResponse<Page<UserResponse>> getPatients(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 5, sort = "id") Pageable pageable) {
    return ApiResponse.<Page<UserResponse>>builder()
        .result(userService.getPatientsForAdmin(search, pageable))
        .build();
  }

  @PatchMapping("/{id}/toggle-lock")
  public ApiResponse<UserResponse> toggleLockPatient(@PathVariable Long id) {
    return ApiResponse.<UserResponse>builder()
        .result(userService.toggleLockPatient(id))
        .build();
  }

  @GetMapping("/{id}/appointments")
  public ApiResponse<Page<com.bookinghealth.api.dto.response.client.AppointmentResponse>> getPatientAppointments(
      @PathVariable Long id,
      @PageableDefault(size = 10, sort = "id") Pageable pageable) {
    return ApiResponse.<Page<com.bookinghealth.api.dto.response.client.AppointmentResponse>>builder()
        .result(appointmentService.getPatientAppointmentsForAdmin(id, pageable))
        .build();
  }
}
