package com.bookinghealth.api.controller;

import com.bookinghealth.api.dto.request.ScreenLogRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.ScreenLogResponse;
import com.bookinghealth.api.service.ScreenLogService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/screen-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScreenLogController {

  ScreenLogService screenLogService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ScreenLogResponse> createScreenLog(
      @RequestBody @Valid ScreenLogRequest request) {
    ScreenLogResponse response = screenLogService.createScreenLog(request);
    return ApiResponse.<ScreenLogResponse>builder()
        .code(HttpStatus.CREATED.value())
        .message("Screen log created successfully")
        .result(response)
        .build();
  }

  /** Người dùng bấm "Phản hồi tốt" -> đánh dấu ca này để dùng làm mẫu huấn luyện AI. */
  @PatchMapping("/{id}/useful")
  public ApiResponse<Void> markUseful(@PathVariable Long id) {
    screenLogService.markUseForTraining(id);
    return ApiResponse.<Void>builder().message("Đã ghi nhận phản hồi tốt").build();
  }
}
