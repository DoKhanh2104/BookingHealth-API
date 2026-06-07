package com.bookinghealth.api.mapper;

import com.bookinghealth.api.dto.response.client.AppointmentResponse;
import com.bookinghealth.api.dto.response.client.ScheduleSlotResponse;
import com.bookinghealth.api.entity.Appointment;
import com.bookinghealth.api.entity.AppointmentSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {DoctorMapper.class, UserMapper.class})
public interface AppointmentMapper {

  @Mapping(source = "doctor", target = "doctor")
  @Mapping(source = "user", target = "user")
  @Mapping(source = "appointmentSlot", target = "appointmentSlot")
  AppointmentResponse toAppointmentResponse(Appointment appointment);

  default ScheduleSlotResponse mapAppointmentSlot(AppointmentSlot slot) {
    if (slot == null) {
      return null;
    }
    var template = slot.getTimeSlotTemplate();
    boolean doctorOpen = slot.getStatus() != null && slot.getStatus() == 1;
    boolean booked =
        slot.getAppointments() != null
            && slot.getAppointments().stream()
                .anyMatch(app -> app.getStatus() != null && app.getStatus() != 3);
    boolean available = doctorOpen && !booked;

    return ScheduleSlotResponse.builder()
        .id(slot.getId())
        .templateId(template != null ? template.getId() : null)
        .startTime(template != null ? template.getStartTime() : null)
        .endTime(template != null ? template.getEndTime() : null)
        .workScheduleId(slot.getWorkSchedule() != null ? slot.getWorkSchedule().getId() : null)
        .doctorOpen(doctorOpen)
        .booked(booked)
        .available(available)
        .build();
  }
}
