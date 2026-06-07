package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.client.DayOffRequest;
import com.bookinghealth.api.dto.response.admin.DayOffResponse;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorDayOff;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.DoctorDayOffRepository;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorDayOffService {

  DoctorDayOffRepository doctorDayOffRepository;
  DoctorRepository doctorRepository;
  UserRepository userRepository;
  NotificationService notificationService;
  WorkScheduleService workScheduleService;

  // ─── Client (Doctor) ───────────────────────────────────────────

  @Transactional
  public DayOffResponse createDayOff(DayOffRequest request) {
    Doctor doctor = getCurrentDoctor();

    DoctorDayOff dayOff =
        DoctorDayOff.builder()
            .doctor(doctor)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .reason(request.getReason())
            .status(0) // PENDING
            .build();

    doctorDayOffRepository.save(dayOff);
    return toResponse(dayOff);
  }

  public List<DayOffResponse> getMyDayOffs() {
    Doctor doctor = getCurrentDoctor();
    return doctorDayOffRepository.findByDoctorIdOrderByIdDesc(doctor.getId()).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  // ─── Admin ─────────────────────────────────────────────────────

  public Page<DayOffResponse> getAllDayOffs(Integer status, Pageable pageable) {
    Page<DoctorDayOff> page;
    if (status != null) {
      page = doctorDayOffRepository.findByStatus(status, pageable);
    } else {
      page = doctorDayOffRepository.findAllByOrderByIdDesc(pageable);
    }
    return page.map(this::toResponse);
  }

  @Transactional
  public DayOffResponse approveDayOff(Long id) {
    DoctorDayOff dayOff =
        doctorDayOffRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.DAY_OFF_NOT_FOUND));
    dayOff.setStatus(1); // APPROVED
    doctorDayOffRepository.save(dayOff);

    // Block schedule
    if (dayOff.getDoctor() != null) {
      workScheduleService.blockScheduleForLeave(
          dayOff.getDoctor().getId(), dayOff.getStartDate(), dayOff.getEndDate());

      // Send notification
      if (dayOff.getDoctor().getUser() != null) {
        String title = "Yêu cầu nghỉ phép được duyệt";
        String content =
            String.format(
                "Yêu cầu nghỉ phép của bạn từ ngày %s đến ngày %s đã được duyệt.",
                dayOff.getStartDate(), dayOff.getEndDate());
        notificationService.createNotification(dayOff.getDoctor().getUser(), title, content, 1);
      }
    }

    return toResponse(dayOff);
  }

  @Transactional
  public DayOffResponse rejectDayOff(Long id) {
    DoctorDayOff dayOff =
        doctorDayOffRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.DAY_OFF_NOT_FOUND));
    dayOff.setStatus(2); // REJECTED
    doctorDayOffRepository.save(dayOff);

    // Send notification
    if (dayOff.getDoctor() != null && dayOff.getDoctor().getUser() != null) {
      String title = "Yêu cầu nghỉ phép bị từ chối";
      String content =
          String.format(
              "Yêu cầu nghỉ phép của bạn từ ngày %s đến ngày %s đã bị từ chối.",
              dayOff.getStartDate(), dayOff.getEndDate());
      notificationService.createNotification(dayOff.getDoctor().getUser(), title, content, 1);
    }

    return toResponse(dayOff);
  }

  // ─── Helpers ───────────────────────────────────────────────────

  private DayOffResponse toResponse(DoctorDayOff dayOff) {
    Doctor doctor = dayOff.getDoctor();
    String doctorName =
        (doctor != null && doctor.getUser() != null) ? doctor.getUser().getName() : "Bác sĩ";
    String clinicName =
        (doctor != null && doctor.getClinic() != null) ? doctor.getClinic().getClinicName() : "";

    return DayOffResponse.builder()
        .id(dayOff.getId())
        .doctorId(doctor != null ? doctor.getId() : null)
        .doctorName(doctorName)
        .clinicName(clinicName)
        .startDate(dayOff.getStartDate())
        .endDate(dayOff.getEndDate())
        .reason(dayOff.getReason())
        .status(dayOff.getStatus())
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
}
