package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.client.BookAppointmentRequest;
import com.bookinghealth.api.dto.request.client.CompleteAppointmentRequest;
import com.bookinghealth.api.dto.response.admin.AppointmentResponse;
import com.bookinghealth.api.dto.response.client.DoctorDashboardResponse;
import com.bookinghealth.api.entity.Appointment;
import com.bookinghealth.api.entity.AppointmentSlot;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.Specialty;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.mapper.AppointmentMapper;
import com.bookinghealth.api.repository.AppointmentRepository;
import com.bookinghealth.api.repository.AppointmentSlotRepository;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentService {

    AppointmentRepository appointmentRepository;
    AppointmentSlotRepository appointmentSlotRepository;
    DoctorRepository doctorRepository;
    UserRepository userRepository;
    AppointmentMapper appointmentMapper;
    NotificationService notificationService;

    public Page<AppointmentResponse> getAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(appointment -> {

            String patientName = appointment.getUser() != null ? appointment.getUser().getName() : "";
            String patientPhone = appointment.getUser() != null ? appointment.getUser().getPhone() : "";


            String doctorName = "";
            String specialty = "";
            if (appointment.getDoctor() != null) {
                if (appointment.getDoctor().getUser() != null) {
                    doctorName = appointment.getDoctor().getUser().getName();
                }
                if (appointment.getDoctor().getSpecialties() != null) {
                    specialty = appointment.getDoctor().getSpecialties().stream()
                            .map(Specialty::getSpecialtyName)
                            .collect(Collectors.joining(", "));
                }
            }

            String appDate = appointment.getExpectedExaminationDate() != null
                    ? appointment.getExpectedExaminationDate().toString()
                    : "";

            String timeSlot = "";
            if (appointment.getAppointmentSlot() != null
                && appointment.getAppointmentSlot().getTimeSlotTemplate() != null) {
                var template = appointment.getAppointmentSlot().getTimeSlotTemplate();
                timeSlot = template.getStartTime() + " - " + template.getEndTime();
            }

            String statusStr = mapStatusToString(appointment.getStatus());
            return AppointmentResponse.builder()
                    .patientName(patientName)
                    .patientPhone(patientPhone)
                    .doctorName(doctorName)
                    .specialty(specialty)
                    .appointmentDate(appDate)
                    .appointmentFee(appointment.getTotalAmount())
                    .status(statusStr)
                    .timeSlot(timeSlot)
                    .build();
        });
    }

    private String mapStatusToString(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case 0: return "PENDING";
            case 1: return "CONFIRMED";
            case 2: return "COMPLETED";
            case 3: return "CANCELLED";
            default: return "PENDING";
        }
    }

    @Transactional
    public com.bookinghealth.api.dto.response.client.AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        User currentUser = getCurrentUser();

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        AppointmentSlot slot = appointmentSlotRepository.findById(request.getAppointmentSlotId())
                .orElseThrow(() -> new AppException(ErrorCode.TIME_SLOT_NOT_FOUND));

        boolean alreadyBooked = appointmentRepository.existsByAppointmentSlotIdAndStatusNotIn(
                slot.getId(), List.of(3));
        if (alreadyBooked) {
            throw new AppException(ErrorCode.TIME_SLOT_BOOKED);
        }

        double fee = 150000.0;
        if (doctor.getPriceHistories() != null && !doctor.getPriceHistories().isEmpty()) {
            fee = doctor.getPriceHistories().stream()
                    .filter(ph -> ph.getStatus() != null && ph.getStatus() == 1)
                    .map(com.bookinghealth.api.entity.PriceHistory::getExaminationFee)
                    .findFirst()
                    .orElse(150000.0);
        }

        Appointment appointment = Appointment.builder()
                .status(0)
                .description(request.getDescription())
                .expectedExaminationDate(request.getExpectedExaminationDate())
                .totalAmount(fee)
                .doctor(doctor)
                .user(currentUser)
                .appointmentSlot(slot)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Push notification to doctor
        if (doctor.getUser() != null) {
            String timeSlotStr = "";
            if (saved.getAppointmentSlot() != null && saved.getAppointmentSlot().getTimeSlotTemplate() != null) {
                var template = saved.getAppointmentSlot().getTimeSlotTemplate();
                timeSlotStr = template.getStartTime() + " - " + template.getEndTime();
            }
            String notifContent = String.format("Bệnh nhân %s đã đặt lịch khám vào lúc %s ngày %s.",
                    currentUser.getName(), timeSlotStr, saved.getExpectedExaminationDate());
            notificationService.createNotification(doctor.getUser(), "Lịch hẹn mới đang chờ duyệt", notifContent, 1);
        }

        return appointmentMapper.toAppointmentResponse(saved);
    }

    public Page<com.bookinghealth.api.dto.response.client.AppointmentResponse> getMyAppointments(
            Integer status, Pageable pageable) {
        User user = getCurrentUser();

        boolean isDoctor = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "DOCTOR".equals(role.getRoleName()));

        Page<Appointment> page;
        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
            if (status != null) {
                page = appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), status, pageable);
            } else {
                page = appointmentRepository.findByDoctorId(doctor.getId(), pageable);
            }
        } else {
            if (status != null) {
                page = appointmentRepository.findByUserIdAndStatus(user.getId(), status, pageable);
            } else {
                page = appointmentRepository.findByUserId(user.getId(), pageable);
            }
        }

        return page.map(appointmentMapper::toAppointmentResponse);
    }

    public com.bookinghealth.api.dto.response.client.AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        User currentUser = getCurrentUser();
        boolean isDoctor = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role -> "DOCTOR".equals(role.getRoleName()));

        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser_Id(currentUser.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            if (!appointment.getUser().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        return appointmentMapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        User currentUser = getCurrentUser();
        boolean isDoctor = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role -> "DOCTOR".equals(role.getRoleName()));

        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser_Id(currentUser.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            if (!appointment.getUser().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        appointment.setStatus(3);
        Appointment saved = appointmentRepository.save(appointment);

        // Push notification to the other party
        String timeSlotStr = "";
        if (saved.getAppointmentSlot() != null && saved.getAppointmentSlot().getTimeSlotTemplate() != null) {
            var template = saved.getAppointmentSlot().getTimeSlotTemplate();
            timeSlotStr = template.getStartTime() + " - " + template.getEndTime();
        }

        if (isDoctor) {
            // Cancelled by Doctor, notify Patient
            if (saved.getUser() != null) {
                String notifContent = String.format("Bác sĩ %s đã hủy lịch hẹn của bạn vào lúc %s ngày %s.",
                        currentUser.getName(), timeSlotStr, saved.getExpectedExaminationDate());
                notificationService.createNotification(saved.getUser(), "Lịch hẹn đã bị hủy", notifContent, 1);
            }
        } else {
            // Cancelled by Patient, notify Doctor
            if (saved.getDoctor() != null && saved.getDoctor().getUser() != null) {
                String notifContent = String.format("Bệnh nhân %s đã hủy lịch hẹn vào lúc %s ngày %s.",
                        currentUser.getName(), timeSlotStr, saved.getExpectedExaminationDate());
                notificationService.createNotification(saved.getDoctor().getUser(), "Lịch hẹn đã bị hủy bởi bệnh nhân", notifContent, 1);
            }
        }
    }

    @Transactional
    public void confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        User currentUser = getCurrentUser();
        Doctor doctor = doctorRepository.findByUser_Id(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (appointment.getStatus() != 0) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        appointment.setStatus(1);
        Appointment saved = appointmentRepository.save(appointment);

        // Push notification to patient
        if (saved.getUser() != null) {
            String timeSlotStr = "";
            if (saved.getAppointmentSlot() != null && saved.getAppointmentSlot().getTimeSlotTemplate() != null) {
                var template = saved.getAppointmentSlot().getTimeSlotTemplate();
                timeSlotStr = template.getStartTime() + " - " + template.getEndTime();
            }
            String notifContent = String.format("Bác sĩ %s đã xác nhận lịch hẹn khám của bạn vào lúc %s ngày %s.",
                    currentUser.getName(), timeSlotStr, saved.getExpectedExaminationDate());
            notificationService.createNotification(saved.getUser(), "Lịch hẹn đã được xác nhận", notifContent, 1);
        }
    }

    @Transactional
    public void completeAppointment(Long id, CompleteAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        User currentUser = getCurrentUser();
        Doctor doctor = doctorRepository.findByUser_Id(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (appointment.getStatus() != 1) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        appointment.setDiagnosis(request.getDiagnosis());
        appointment.setMedicine(request.getMedicine());
        appointment.setAttachment(request.getAttachment());
        appointment.setStatus(2);
        appointment.setActualArrivalTime(LocalDate.now());

        Appointment saved = appointmentRepository.save(appointment);

        // Push notification to patient
        if (saved.getUser() != null) {
            String timeSlotStr = "";
            if (saved.getAppointmentSlot() != null && saved.getAppointmentSlot().getTimeSlotTemplate() != null) {
                var template = saved.getAppointmentSlot().getTimeSlotTemplate();
                timeSlotStr = template.getStartTime() + " - " + template.getEndTime();
            }
            String notifContent = String.format("Kết quả ca khám của bạn vào lúc %s ngày %s đã có. Vui lòng xem bệnh án và đơn thuốc chi tiết.",
                    timeSlotStr, saved.getExpectedExaminationDate());
            notificationService.createNotification(saved.getUser(), "Kết quả khám bệnh đã sẵn sàng", notifContent, 1);
        }
    }

    public DoctorDashboardResponse getDoctorDashboardStats() {
        User currentUser = getCurrentUser();
        Doctor doctor = doctorRepository.findByUser_Id(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        LocalDate today = LocalDate.now();
        List<Appointment> allDoctorAppointments = appointmentRepository.findByDoctorId(doctor.getId());
        List<Appointment> todayAppointments = appointmentRepository.findByDoctorIdAndExpectedExaminationDate(doctor.getId(), today);

        int todayTotal = todayAppointments.size();
        int todayPending = (int) todayAppointments.stream().filter(app -> app.getStatus() == 0).count();
        int todayCompleted = (int) todayAppointments.stream().filter(app -> app.getStatus() == 2).count();

        int uniquePatients = (int) allDoctorAppointments.stream()
                .filter(app -> app.getStatus() == 1 || app.getStatus() == 2)
                .map(app -> app.getUser().getId())
                .distinct()
                .count();

        double avgRating = 5.0;
        int reviewCount = 0;
        if (doctor.getReviews() != null && !doctor.getReviews().isEmpty()) {
            reviewCount = doctor.getReviews().size();
            double sum = doctor.getReviews().stream()
                    .mapToDouble(r -> r.getRating() != null ? r.getRating() : 5.0)
                    .sum();
            avgRating = Math.round((sum / reviewCount) * 10.0) / 10.0;
        }

        double monthlyRevenue = allDoctorAppointments.stream()
                .filter(app -> app.getStatus() == 2)
                .filter(app -> app.getExpectedExaminationDate() != null
                        && app.getExpectedExaminationDate().getMonth() == today.getMonth()
                        && app.getExpectedExaminationDate().getYear() == today.getYear())
                .mapToDouble(app -> app.getTotalAmount() != null ? app.getTotalAmount() : 0.0)
                .sum();

        List<com.bookinghealth.api.dto.response.client.AppointmentResponse> featured = todayAppointments.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());

        return DoctorDashboardResponse.builder()
                .todayAppointmentsCount(todayTotal)
                .todayPendingCount(todayPending)
                .todayCompletedCount(todayCompleted)
                .totalPatientsCount(uniquePatients)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .monthlyRevenue(monthlyRevenue)
                .todayFeaturedAppointments(featured)
                .build();
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String identifier = authentication.getName();
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public Page<com.bookinghealth.api.dto.response.client.AppointmentResponse> getPatientAppointmentsForAdmin(
            Long patientId, Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findByUserId(patientId, pageable);
        return page.map(appointmentMapper::toAppointmentResponse);
    }
}
