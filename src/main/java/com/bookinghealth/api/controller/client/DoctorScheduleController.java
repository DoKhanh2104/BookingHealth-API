package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.client.ScheduleSlotResponse;
import com.bookinghealth.api.service.WorkScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors/me")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorScheduleController {

  WorkScheduleService workScheduleService;

  @PatchMapping("/schedule-slots/{slotId}")
  public ApiResponse<ScheduleSlotResponse> toggleScheduleSlot(@PathVariable Long slotId) {
    return ApiResponse.<ScheduleSlotResponse>builder()
        .result(workScheduleService.toggleSlotForCurrentDoctor(slotId))
        .build();
  }
}
