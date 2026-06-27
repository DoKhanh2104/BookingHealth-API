package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.client.DayOffRequest;
import com.bookinghealth.api.dto.response.admin.DayOffResponse;
import com.bookinghealth.api.entity.Appointment;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorDayOff;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.AppointmentRepository;
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
  AppointmentRepository appointmentRepository;

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

    // Báo cho admin có đơn nghỉ phép mới cần duyệt
    String doctorName = (doctor.getUser() != null) ? doctor.getUser().getName() : "Bác sĩ";
    notificationService.notifyAdmins(
        "Đơn nghỉ phép mới chờ duyệt",
        String.format(
            "Bác sĩ %s vừa gửi đơn xin nghỉ phép từ %s đến %s. Vui lòng kiểm tra mục Quản lý lịch.",
            doctorName, request.getStartDate(), request.getEndDate()),
        2);

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

    // 1. Block schedule (lịch làm việc)
    if (dayOff.getDoctor() != null) {
      workScheduleService.blockScheduleForLeave(
          dayOff.getDoctor().getId(), dayOff.getStartDate(), dayOff.getEndDate());

      // 2. Tìm tất cả lịch hẹn bị ảnh hưởng (PENDING=0, CONFIRMED=1)
      List<Appointment> affected =
          appointmentRepository.findByDoctorIdAndExpectedExaminationDateBetweenAndStatusIn(
              dayOff.getDoctor().getId(),
              dayOff.getStartDate(),
              dayOff.getEndDate(),
              List.of(0, 1));

      // 3. Hủy từng lịch hẹn và thông báo bệnh nhân
      int cancelledCount = 0;
      String doctorName =
          dayOff.getDoctor().getUser() != null
              ? dayOff.getDoctor().getUser().getName()
              : "Bác sĩ";

      for (Appointment appt : affected) {
        appt.setStatus(3); // CANCELLED
        appointmentRepository.save(appt);
        cancelledCount++;

        // Thông báo cho bệnh nhân
        if (appt.getUser() != null) {
          String timeSlotStr = "";
          if (appt.getAppointmentSlot() != null
              && appt.getAppointmentSlot().getTimeSlotTemplate() != null) {
            var tpl = appt.getAppointmentSlot().getTimeSlotTemplate();
            timeSlotStr = tpl.getStartTime() + " - " + tpl.getEndTime();
          }
          String patientTitle = "Lịch khám bị hủy do bác sĩ nghỉ";
          String patientContent =
              String.format(
                  "Bác sĩ %s đã được phép nghỉ từ ngày %s đến ngày %s. "
                      + "Lịch hẹn của bạn vào lúc %s ngày %s đã bị hủy tự động. "
                      + "Vui lòng đặt lại lịch với bác sĩ khác. Xin lỗi vì sự bất tiện này!",
                  doctorName,
                  dayOff.getStartDate(),
                  dayOff.getEndDate(),
                  timeSlotStr,
                  appt.getExpectedExaminationDate());
          notificationService.createNotification(appt.getUser(), patientTitle, patientContent, 1);
        }
      }

      // 4. Thông báo tổng kết cho bác sĩ
      if (dayOff.getDoctor().getUser() != null) {
        String doctorTitle = "Yêu cầu nghỉ phép được duyệt";
        String doctorContent;
        if (cancelledCount > 0) {
          doctorContent =
              String.format(
                  "Yêu cầu nghỉ phép của bạn từ ngày %s đến ngày %s đã được duyệt. "
                      + "Hệ thống đã tự động hủy %d lịch hẹn bị ảnh hưởng và thông báo đến từng bệnh nhân.",
                  dayOff.getStartDate(), dayOff.getEndDate(), cancelledCount);
        } else {
          doctorContent =
              String.format(
                  "Yêu cầu nghỉ phép của bạn từ ngày %s đến ngày %s đã được duyệt. "
                      + "Không có lịch hẹn nào bị ảnh hưởng trong giai đoạn này.",
                  dayOff.getStartDate(), dayOff.getEndDate());
        }
        notificationService.createNotification(
            dayOff.getDoctor().getUser(), doctorTitle, doctorContent, 1);
      }
    }

    return toResponse(dayOff);
  }

  @Transactional
  public DayOffResponse rejectDayOff(Long id, String reason) {
    DoctorDayOff dayOff =
        doctorDayOffRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.DAY_OFF_NOT_FOUND));
    dayOff.setStatus(2); // REJECTED
    dayOff.setRejectReason(reason);
    doctorDayOffRepository.save(dayOff);

    // Send notification (kèm lý do nếu admin có nhập)
    if (dayOff.getDoctor() != null && dayOff.getDoctor().getUser() != null) {
      String title = "Yêu cầu nghỉ phép bị từ chối";
      String content =
          String.format(
              "Yêu cầu nghỉ phép của bạn từ ngày %s đến ngày %s đã bị từ chối.",
              dayOff.getStartDate(), dayOff.getEndDate());
      if (reason != null && !reason.isBlank()) {
        content += " Lý do: " + reason;
      }
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
        .rejectReason(dayOff.getRejectReason())
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
