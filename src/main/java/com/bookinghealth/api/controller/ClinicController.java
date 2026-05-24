package com.bookinghealth.api.controller;

import com.bookinghealth.api.dto.request.admin.ClinicCreateRequest;
import com.bookinghealth.api.dto.request.admin.GeocodingRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.GeocodingResponse;
import com.bookinghealth.api.dto.response.admin.ClinicAdminResponse;
import com.bookinghealth.api.entity.Clinic;
import com.bookinghealth.api.service.ClinicService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/clinics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClinicController {

  ClinicService clinicService;

  @GetMapping("/geocode")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<GeocodingResponse> geocode(GeocodingRequest request) {

    return ApiResponse.<GeocodingResponse>builder()
        .result(clinicService.getGeocoding(request))
        .build();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> createClinic(@Valid @RequestBody ClinicCreateRequest request) {
    boolean saved = clinicService.saveClinic(request);
    if (!saved) {
      return ApiResponse.<Void>builder()
          .code(409)
          .message("Phòng khám đã tồn tại (trùng tên + địa chỉ)")
          .build();
    }
    return ApiResponse.<Void>builder().build();
  }

  @GetMapping
  public ApiResponse<Page<ClinicAdminResponse>> getAllClinics(@PageableDefault(size = 10) Pageable pageable) {
    return ApiResponse.<Page<ClinicAdminResponse>>builder()
        .result(clinicService.getAllClinics(pageable))
        .build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<ClinicAdminResponse> updateClinic(
      @PathVariable Long id, @Valid @RequestBody ClinicCreateRequest request) {
    return ApiResponse.<ClinicAdminResponse>builder()
        .result(clinicService.updateClinic(id, request))
        .build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> deleteClinic(@PathVariable Long id) {
    clinicService.deleteClinic(id);
    return ApiResponse.<Void>builder().build();
  }
}
