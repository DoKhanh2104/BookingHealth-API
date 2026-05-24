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
}
