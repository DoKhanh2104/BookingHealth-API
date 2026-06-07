package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.request.client.DayOffRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.DayOffResponse;
import com.bookinghealth.api.service.DoctorDayOffService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors/me/day-offs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorDayOffController {

  DoctorDayOffService doctorDayOffService;

  @PostMapping
  public ApiResponse<DayOffResponse> createDayOff(@RequestBody DayOffRequest request) {
    return ApiResponse.<DayOffResponse>builder()
        .result(doctorDayOffService.createDayOff(request))
        .build();
  }

  @GetMapping
  public ApiResponse<List<DayOffResponse>> getMyDayOffs() {
    return ApiResponse.<List<DayOffResponse>>builder()
        .result(doctorDayOffService.getMyDayOffs())
        .build();
  }
}
