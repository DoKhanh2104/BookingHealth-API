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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/clinics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClinicController {

  ClinicService clinicService;

  @GetMapping("/geocode")
  public ApiResponse<GeocodingResponse> geocode(GeocodingRequest request) {

    return ApiResponse.<GeocodingResponse>builder()
        .result(clinicService.getGeocoding(request))
        .build();
  }

  @PostMapping
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
}
