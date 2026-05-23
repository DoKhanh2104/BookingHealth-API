package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedStatusTimeSlot;
import com.bookinghealth.api.dto.request.admin.AppointmentSlotRequest;
import com.bookinghealth.api.dto.response.admin.AppointmentSlotResponse;
import com.bookinghealth.api.entity.AppointmentSlot;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.AppointmentSlotRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentSlotService {

    AppointmentSlotRepository appointmentSlotRepository;

    public AppointmentSlotResponse createTimeSlot (AppointmentSlotRequest request) {

        if(request.getStartTime().isAfter(request.getEndTime())){
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if(appointmentSlotRepository.existsByStartTimeAndEndTime(request.getStartTime(), request.getEndTime())) {
            throw new AppException(ErrorCode.TIME_SLOT_EXISTED);
        }

        // Map request data to AppointmentSlot entity
        AppointmentSlot slot = AppointmentSlot.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(PredefinedStatusTimeSlot.ACTIVE)
                .build();

        // Save entity
        AppointmentSlot saved = appointmentSlotRepository.save(slot);

        // Map entity to response DTO
        return AppointmentSlotResponse.builder()
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .build();
    }

    public void updateTimeSlotStatus(Long id) {
        AppointmentSlot slot = appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TIME_SLOT_NOT_FOUND));

        slot.setStatus(slot.getStatus() == PredefinedStatusTimeSlot.ACTIVE
                ? PredefinedStatusTimeSlot.INACTIVE
                : PredefinedStatusTimeSlot.ACTIVE);

        appointmentSlotRepository.save(slot);
    }

    public Page<AppointmentSlot> getAllTimeSlots(Pageable pageable) {
        return appointmentSlotRepository.findAll(pageable);
    }

    public void deleteTimeSlot(Long id) {
        appointmentSlotRepository.deleteById(id);
    }
}
