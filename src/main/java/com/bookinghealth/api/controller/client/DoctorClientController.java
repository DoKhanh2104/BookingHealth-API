package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.client.DoctorResponse;
import com.bookinghealth.api.service.DoctorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/doctors")
public class DoctorClientController {

  DoctorService doctorService;

  @GetMapping
  public ApiResponse<Page<DoctorResponse>> getDoctors(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) Long specialtyId,
      @RequestParam(required = false) Long clinicId,
      @RequestParam(required = false) String search) {
    return ApiResponse.<Page<DoctorResponse>>builder()
        .result(doctorService.getDoctorsForClient(page, size, specialtyId, clinicId, search))
        .build();
  }

  @GetMapping("/{id}")
  public ApiResponse<DoctorResponse> getDoctorById(@PathVariable Long id) {
    return ApiResponse.<DoctorResponse>builder()
        .result(doctorService.getDoctorByIdForClient(id))
        .build();
  }

  @GetMapping("/{id}/work-schedules")
  public ApiResponse<java.util.List<com.bookinghealth.api.dto.response.client.WorkScheduleResponse>>
      getWorkSchedules(@PathVariable Long id, @RequestParam String date) {
    return ApiResponse
        .<java.util.List<com.bookinghealth.api.dto.response.client.WorkScheduleResponse>>builder()
        .result(doctorService.getWorkSchedules(id, date))
        .build();
  }

  @GetMapping("/{id}/reviews")
  public ApiResponse<org.springframework.data.domain.Page<com.bookinghealth.api.dto.response.client.DoctorReviewResponse>>
      getDoctorReviews(
          @PathVariable Long id,
          @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
    return ApiResponse
        .<org.springframework.data.domain.Page<com.bookinghealth.api.dto.response.client.DoctorReviewResponse>>builder()
        .result(doctorService.getReviewsForDoctor(id, pageable))
        .build();
  }
}
