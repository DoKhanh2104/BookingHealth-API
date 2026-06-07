package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.request.client.CreateReviewRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.client.DoctorReviewResponse;
import com.bookinghealth.api.service.DoctorReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/reviews")
public class DoctorReviewController {

  DoctorReviewService doctorReviewService;

  @PostMapping
  public ApiResponse<DoctorReviewResponse> createReview(
      @Valid @RequestBody CreateReviewRequest request) {
    return ApiResponse.<DoctorReviewResponse>builder()
        .result(doctorReviewService.createReview(request))
        .build();
  }
}
