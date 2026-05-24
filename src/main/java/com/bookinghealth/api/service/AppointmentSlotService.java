package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedStatusTimeSlot;
import com.bookinghealth.api.dto.request.admin.AppointmentSlotRequest;
import com.bookinghealth.api.dto.response.admin.AppointmentSlotResponse;
import com.bookinghealth.api.entity.TimeSlotTemplate;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.TimeSlotTemplateRepository;
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

  TimeSlotTemplateRepository timeSlotTemplateRepository;

  public AppointmentSlotResponse createTimeSlot(AppointmentSlotRequest request) {
    if (request.getStartTime().isAfter(request.getEndTime())) {
      throw new IllegalArgumentException("Start time must be before end time");
    }

    if (timeSlotTemplateRepository.existsByStartTimeAndEndTime(
        request.getStartTime(), request.getEndTime())) {
      throw new AppException(ErrorCode.TIME_SLOT_EXISTED);
    }

    TimeSlotTemplate saved =
        timeSlotTemplateRepository.save(
            TimeSlotTemplate.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(PredefinedStatusTimeSlot.ACTIVE)
                .build());

    return toResponse(saved);
  }

  public void updateTimeSlotStatus(Long id) {
    TimeSlotTemplate template =
        timeSlotTemplateRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.TIME_SLOT_NOT_FOUND));

    template.setStatus(
        template.getStatus() == PredefinedStatusTimeSlot.ACTIVE
            ? PredefinedStatusTimeSlot.INACTIVE
            : PredefinedStatusTimeSlot.ACTIVE);

    timeSlotTemplateRepository.save(template);
  }

  public Page<AppointmentSlotResponse> getAllTimeSlots(Pageable pageable) {
    return timeSlotTemplateRepository.findAll(pageable).map(this::toResponse);
  }

  public void deleteTimeSlot(Long id) {
    timeSlotTemplateRepository.deleteById(id);
  }

  private AppointmentSlotResponse toResponse(TimeSlotTemplate template) {
    return AppointmentSlotResponse.builder()
        .id(template.getId())
        .startTime(template.getStartTime())
        .endTime(template.getEndTime())
        .status(template.getStatus())
        .build();
  }
}
