package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.HealthDepartmentRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.service.HealthDepartmentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/health-departments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthDepartmentController {

  HealthDepartmentService healthDepartmentService;

  @PostMapping
  public ApiResponse<Void> saveRawData(@RequestBody HealthDepartmentRequest request) {
    healthDepartmentService.saveRawData(request);
    return ApiResponse.<Void>builder().build();
  }

  @PostMapping("/sync-missing-clinics")
  public ApiResponse<Integer> syncMissingClinics(@RequestParam(defaultValue = "20") int batchSize) {
    int created = healthDepartmentService.syncMissingClinics(batchSize);
    return ApiResponse.<Integer>builder().result(created).build();
  }
}
