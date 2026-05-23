package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.AppointmentSlotRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.AppointmentSlotResponse;
import com.bookinghealth.api.entity.AppointmentSlot;
import com.bookinghealth.api.service.AppointmentSlotService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/appointment-slots")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentSlotController {

    AppointmentSlotService appointmentSlotService;

    @PostMapping
    public ApiResponse<AppointmentSlotResponse> createTimeSlot(@RequestBody AppointmentSlotRequest request) {
        return ApiResponse.<AppointmentSlotResponse>builder()
                .result(appointmentSlotService.createTimeSlot(request))
                .build();
    }
    
    @PatchMapping("/{id}")
    public ApiResponse<Void> updateTimeSlot(@PathVariable Long id) {
        appointmentSlotService.updateTimeSlotStatus(id);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping
    public ApiResponse<Page<AppointmentSlot>> getTimeSlots(Pageable pageable) {
        return ApiResponse.<Page<AppointmentSlot>>builder()
                .result(appointmentSlotService.getAllTimeSlots(pageable))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTimeSlot(@PathVariable Long id){
        appointmentSlotService.deleteTimeSlot(id);
        return ApiResponse.<Void>builder()
                .build();
    }

}
