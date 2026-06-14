package com.bookinghealth.api.service;

import com.bookinghealth.api.entity.Appointment;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.repository.AppointmentRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled tasks cho hệ thống thông báo BookingHealth.
 *
 * <p>Job 1 — Nhắc bác sĩ điều chỉnh lịch tuần tới:<br>
 * Chạy mỗi tối thứ 7 lúc 20:00 (GMT+7). Gửi thông báo cho TẤT CẢ bác sĩ
 * nhắc họ kiểm tra và cập nhật lịch khám cho tuần tới.
 *
 * <p>Job 2 — Nhắc hẹn trước 1 ngày:<br>
 * Chạy mỗi ngày lúc 08:00 (GMT+7). Tìm các lịch hẹn đang PENDING (0) hoặc
 * CONFIRMED (1) vào ngày hôm sau và gửi nhắc nhở cho cả bác sĩ lẫn bệnh nhân.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationSchedulerService {

  NotificationService notificationService;
  AppointmentRepository appointmentRepository;
  UserRepository userRepository;

  // ─────────────────────────────────────────────────────────────────────────
  // JOB 1: Nhắc bác sĩ cập nhật lịch — mỗi tối thứ 7 lúc 20:00 (GMT+7)
  // Cron: 0 0 20 * * SAT  (giây phút giờ ngày tháng thứ)
  // ─────────────────────────────────────────────────────────────────────────

  @Scheduled(cron = "0 0 20 * * SAT", zone = "Asia/Ho_Chi_Minh")
  @Transactional
  public void sendWeeklyScheduleReminderToDoctors() {
    log.info("[Scheduler] Bắt đầu gửi nhắc lịch tuần cho bác sĩ (thứ 7 tối)...");

    // Lấy tất cả user có role DOCTOR
    List<User> doctors =
        userRepository.findAll().stream()
            .filter(
                u ->
                    u.getRoles() != null
                        && u.getRoles().stream()
                            .anyMatch(r -> "DOCTOR".equals(r.getRoleName())))
            .toList();

    if (doctors.isEmpty()) {
      log.info("[Scheduler] Không có bác sĩ nào trong hệ thống.");
      return;
    }

    LocalDate nextMonday = LocalDate.now().plusDays(2);  // Thứ 7 + 2 = Thứ 2
    LocalDate nextSunday = nextMonday.plusDays(6);

    String title = "Nhắc nhở: Cập nhật lịch khám tuần tới";
    String content =
        String.format(
            "Xin chào, đây là thông báo nhắc nhở từ BookingHealth. "
                + "Vui lòng kiểm tra và cập nhật lịch làm việc của bạn cho tuần từ %s đến %s. "
                + "Hãy đảm bảo lịch trống được thiết lập đầy đủ để bệnh nhân có thể đặt hẹn kịp thời. "
                + "Cảm ơn bạn đã đồng hành cùng BookingHealth!",
            nextMonday, nextSunday);

    int successCount = 0;
    for (User doctor : doctors) {
      try {
        // type = 2: loại thông báo nhắc lịch (phân biệt với type=1 là lịch hẹn)
        notificationService.createNotification(doctor, title, content, 2);
        successCount++;
      } catch (Exception e) {
        log.warn(
            "[Scheduler] Không thể gửi nhắc lịch cho bác sĩ userId={}: {}",
            doctor.getId(),
            e.getMessage());
      }
    }

    log.info(
        "[Scheduler] Đã gửi nhắc lịch tuần cho {}/{} bác sĩ.", successCount, doctors.size());
  }

  // ─────────────────────────────────────────────────────────────────────────
  // JOB 2: Nhắc hẹn trước 1 ngày — chạy mỗi ngày lúc 08:00 sáng (GMT+7)
  // Cron: 0 0 8 * * *
  // ─────────────────────────────────────────────────────────────────────────

  @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
  @Transactional
  public void sendAppointmentReminderOneDayBefore() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    log.info("[Scheduler] Gửi nhắc hẹn trước 1 ngày cho ngày: {}", tomorrow);

    // Chỉ nhắc lịch hẹn PENDING (0) hoặc CONFIRMED (1) — bỏ qua COMPLETED/CANCELLED
    List<Appointment> appointments =
        appointmentRepository.findByExpectedExaminationDateAndStatusIn(
            tomorrow, List.of(0, 1));

    if (appointments.isEmpty()) {
      log.info("[Scheduler] Không có lịch hẹn nào vào ngày {}.", tomorrow);
      return;
    }

    log.info("[Scheduler] Tìm thấy {} lịch hẹn vào ngày {}.", appointments.size(), tomorrow);

    int patientCount = 0;
    int doctorCount = 0;

    for (Appointment appointment : appointments) {
      String timeSlotStr = buildTimeSlotStr(appointment);

      // ── Nhắc bệnh nhân ──
      if (appointment.getUser() != null) {
        String doctorName =
            (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null)
                ? appointment.getDoctor().getUser().getName()
                : "bác sĩ";

        String patientContent =
            String.format(
                "Nhắc nhở: Bạn có lịch hẹn khám với Bác sĩ %s vào lúc %s ngày mai (%s). "
                    + "Vui lòng đến đúng giờ. Nếu cần hủy, hãy thực hiện trước ít nhất 4 tiếng.",
                doctorName, timeSlotStr, tomorrow);

        try {
          notificationService.createNotification(
              appointment.getUser(), "Nhắc lịch hẹn khám ngày mai", patientContent, 1);
          patientCount++;
        } catch (Exception e) {
          log.warn(
              "[Scheduler] Không thể gửi nhắc cho bệnh nhân appointmentId={}: {}",
              appointment.getId(),
              e.getMessage());
        }
      }

      // ── Nhắc bác sĩ ──
      if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null) {
        String patientName =
            appointment.getUser() != null ? appointment.getUser().getName() : "bệnh nhân";

        String statusLabel = appointment.getStatus() == 0 ? "chờ duyệt" : "đã xác nhận";

        String doctorContent =
            String.format(
                "Nhắc nhở: Bạn có lịch khám với bệnh nhân %s vào lúc %s ngày mai (%s) "
                    + "(trạng thái: %s). Vui lòng kiểm tra và xác nhận nếu chưa duyệt.",
                patientName, timeSlotStr, tomorrow, statusLabel);

        try {
          notificationService.createNotification(
              appointment.getDoctor().getUser(),
              "Nhắc lịch khám bệnh nhân ngày mai",
              doctorContent,
              1);
          doctorCount++;
        } catch (Exception e) {
          log.warn(
              "[Scheduler] Không thể gửi nhắc cho bác sĩ appointmentId={}: {}",
              appointment.getId(),
              e.getMessage());
        }
      }
    }

    log.info(
        "[Scheduler] Hoàn tất nhắc hẹn ngày {}: {} bệnh nhân, {} bác sĩ.",
        tomorrow, patientCount, doctorCount);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────────────────────

  private String buildTimeSlotStr(Appointment appointment) {
    if (appointment.getAppointmentSlot() == null
        || appointment.getAppointmentSlot().getTimeSlotTemplate() == null) {
      return "giờ đã đăng ký";
    }
    var template = appointment.getAppointmentSlot().getTimeSlotTemplate();
    return template.getStartTime() + " - " + template.getEndTime();
  }
}
