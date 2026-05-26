package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.request.client.BookAppointmentRequest;
import com.bookinghealth.api.dto.request.client.CompleteAppointmentRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.client.AppointmentResponse;
import com.bookinghealth.api.dto.response.client.DoctorDashboardResponse;
import com.bookinghealth.api.service.AppointmentService;
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
@RequestMapping("/appointments")
public class AppointmentClientController {

  AppointmentService appointmentService;

  @PostMapping
  public ApiResponse<AppointmentResponse> bookAppointment(
      @Valid @RequestBody BookAppointmentRequest request) {
    return ApiResponse.<AppointmentResponse>builder()
        .result(appointmentService.bookAppointment(request))
        .build();
  }

  @GetMapping("/me")
  public ApiResponse<Page<AppointmentResponse>> getMyAppointments(
      @RequestParam(required = false) Integer status,
      @PageableDefault(size = 10) Pageable pageable) {
    return ApiResponse.<Page<AppointmentResponse>>builder()
        .result(appointmentService.getMyAppointments(status, pageable))
        .build();
  }

  @GetMapping("/{id}")
  public ApiResponse<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
    return ApiResponse.<AppointmentResponse>builder()
        .result(appointmentService.getAppointmentById(id))
        .build();
  }

  @PutMapping("/{id}/cancel")
  public ApiResponse<Void> cancelAppointment(@PathVariable Long id) {
    appointmentService.cancelAppointment(id);
    return ApiResponse.<Void>builder().build();
  }

  @PutMapping("/{id}/confirm")
  public ApiResponse<Void> confirmAppointment(@PathVariable Long id) {
    appointmentService.confirmAppointment(id);
    return ApiResponse.<Void>builder().build();
  }

  @PutMapping("/{id}/complete")
  public ApiResponse<Void> completeAppointment(
      @PathVariable Long id, @Valid @RequestBody CompleteAppointmentRequest request) {
    appointmentService.completeAppointment(id, request);
    return ApiResponse.<Void>builder().build();
  }

  @GetMapping("/doctor/dashboard")
  public ApiResponse<DoctorDashboardResponse> getDoctorDashboardStats() {
    return ApiResponse.<DoctorDashboardResponse>builder()
        .result(appointmentService.getDoctorDashboardStats())
        .build();
  }
}
