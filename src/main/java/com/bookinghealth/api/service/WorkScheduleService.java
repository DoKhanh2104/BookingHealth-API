package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedStatusTimeSlot;
import com.bookinghealth.api.dto.response.client.ScheduleSlotResponse;
import com.bookinghealth.api.dto.response.client.WorkScheduleResponse;
import com.bookinghealth.api.entity.AppointmentSlot;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.TimeSlotTemplate;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.entity.WorkSchedule;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.AppointmentRepository;
import com.bookinghealth.api.repository.AppointmentSlotRepository;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.TimeSlotTemplateRepository;
import com.bookinghealth.api.repository.UserRepository;
import com.bookinghealth.api.repository.WorkScheduleRepository;
import com.bookinghealth.api.dto.response.admin.WorkScheduleAdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkScheduleService {

  private static final List<Integer> CANCELLED_STATUSES = List.of(3);

  WorkScheduleRepository workScheduleRepository;
  AppointmentSlotRepository appointmentSlotRepository;
  TimeSlotTemplateRepository timeSlotTemplateRepository;
  DoctorRepository doctorRepository;
  UserRepository userRepository;
  AppointmentRepository appointmentRepository;

  @Transactional
  public List<WorkScheduleResponse> getWorkSchedulesForClient(Long doctorId, String dateStr) {
    LocalDate date = LocalDate.parse(dateStr);
    WorkSchedule schedule = getOrCreateWorkSchedule(doctorId, date);
    ensureScheduleSlots(schedule);
    return List.of(toResponse(schedule, true));
  }

  @Transactional
  public ScheduleSlotResponse toggleSlotForCurrentDoctor(Long slotId) {
    Doctor doctor = getCurrentDoctor();
    AppointmentSlot slot =
        appointmentSlotRepository
            .findByIdWithDetails(slotId)
            .orElseThrow(() -> new AppException(ErrorCode.TIME_SLOT_NOT_FOUND));

    if (!slot.getWorkSchedule().getDoctor().getId().equals(doctor.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    if (appointmentRepository.existsByAppointmentSlotIdAndStatusNotIn(
        slotId, CANCELLED_STATUSES)) {
      throw new AppException(ErrorCode.TIME_SLOT_BOOKED);
    }

    int next =
        slot.getStatus() == PredefinedStatusTimeSlot.ACTIVE
            ? PredefinedStatusTimeSlot.INACTIVE
            : PredefinedStatusTimeSlot.ACTIVE;
    slot.setStatus(next);
    appointmentSlotRepository.save(slot);
    return toSlotResponse(slot, slot.getWorkSchedule().getId(), false);
  }

  @Transactional
  public WorkSchedule getOrCreateWorkSchedule(Long doctorId, LocalDate date) {
    return workScheduleRepository
        .findByDoctorIdAndWorkDate(doctorId, date)
        .orElseGet(
            () -> {
              Doctor doctor =
                  doctorRepository
                      .findById(doctorId)
                      .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
              WorkSchedule created =
                  WorkSchedule.builder()
                      .doctor(doctor)
                      .workDate(date)
                      .status(PredefinedStatusTimeSlot.ACTIVE)
                      .build();
              return workScheduleRepository.save(created);
            });
  }

  @Transactional
  public void ensureScheduleSlots(WorkSchedule schedule) {
    List<TimeSlotTemplate> templates =
        timeSlotTemplateRepository.findByStatusOrderByStartTimeAsc(
            PredefinedStatusTimeSlot.ACTIVE);

    for (TimeSlotTemplate template : templates) {
      if (!appointmentSlotRepository.existsByWorkScheduleIdAndTimeSlotTemplateId(
          schedule.getId(), template.getId())) {
        appointmentSlotRepository.save(
            AppointmentSlot.builder()
                .workSchedule(schedule)
                .timeSlotTemplate(template)
                .status(PredefinedStatusTimeSlot.ACTIVE)
                .build());
      }
    }
  }

  private WorkScheduleResponse toResponse(WorkSchedule schedule, boolean forPatient) {
    List<AppointmentSlot> slots =
        appointmentSlotRepository.findByWorkScheduleIdWithActiveTemplates(
            schedule.getId(), PredefinedStatusTimeSlot.ACTIVE);

    List<ScheduleSlotResponse> slotResponses =
        slots.stream()
            .map(s -> toSlotResponse(s, schedule.getId(), forPatient))
            .collect(Collectors.toList());

    return WorkScheduleResponse.builder()
        .id(schedule.getId())
        .doctorId(schedule.getDoctor().getId())
        .workDate(schedule.getWorkDate())
        .slots(slotResponses)
        .build();
  }

  private ScheduleSlotResponse toSlotResponse(
      AppointmentSlot slot, Long workScheduleId, boolean forPatient) {
    TimeSlotTemplate template = slot.getTimeSlotTemplate();
    boolean doctorOpen = slot.getStatus() == PredefinedStatusTimeSlot.ACTIVE;
    boolean booked =
        appointmentRepository.existsByAppointmentSlotIdAndStatusNotIn(
            slot.getId(), CANCELLED_STATUSES);
    boolean available = doctorOpen && !booked;

    return ScheduleSlotResponse.builder()
        .id(slot.getId())
        .templateId(template.getId())
        .startTime(template.getStartTime())
        .endTime(template.getEndTime())
        .workScheduleId(workScheduleId)
        .doctorOpen(doctorOpen)
        .booked(booked)
        .available(forPatient ? available : doctorOpen)
        .build();
  }

  private Doctor getCurrentDoctor() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    String identifier = authentication.getName();
    User user =
        userRepository
            .findByEmail(identifier)
            .or(() -> userRepository.findByPhone(identifier))
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    return doctorRepository
        .findByUser_Id(user.getId())
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
  }

  @Transactional
  public Page<WorkScheduleAdminResponse> getWorkSchedulesForAdmin(
      String dateStr, Long clinicId, Long doctorId, Pageable pageable) {
    LocalDate date = LocalDate.parse(dateStr);
    Page<Doctor> doctorsPage = doctorRepository.findActiveDoctorsForSchedule(clinicId, doctorId, pageable);
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

    return doctorsPage.map(doctor -> {
      WorkSchedule schedule = getOrCreateWorkSchedule(doctor.getId(), date);
      ensureScheduleSlots(schedule);

      List<AppointmentSlot> activeSlots = appointmentSlotRepository.findByWorkScheduleIdWithActiveTemplates(
          schedule.getId(), PredefinedStatusTimeSlot.ACTIVE);

      List<String> activeTimeSlots = activeSlots.stream()
          .filter(slot -> slot.getStatus() == PredefinedStatusTimeSlot.ACTIVE)
          .map(slot -> {
            TimeSlotTemplate template = slot.getTimeSlotTemplate();
            return template.getStartTime().format(formatter) + " - " + template.getEndTime().format(formatter);
          })
          .collect(Collectors.toList());

      return WorkScheduleAdminResponse.builder()
          .id(schedule.getId())
          .doctorId(doctor.getId())
          .doctorName(doctor.getUser().getName())
          .clinicId(doctor.getClinic() != null ? doctor.getClinic().getId() : null)
          .clinicName(doctor.getClinic() != null ? doctor.getClinic().getClinicName() : "")
          .date(date)
          .timeSlots(activeTimeSlots)
          .build();
    });
  }
}
