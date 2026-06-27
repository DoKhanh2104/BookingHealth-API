package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.admin.DoctorStatusUpdateRequest;
import com.bookinghealth.api.dto.response.admin.DoctorAdminResponse;
import com.bookinghealth.api.dto.response.client.DoctorResponse;
import com.bookinghealth.api.dto.response.client.MyDoctorApplicationResponse;
import com.bookinghealth.api.dto.response.client.WorkScheduleResponse;
import com.bookinghealth.api.dto.request.client.QualificationRequest;
import com.bookinghealth.api.dto.request.client.UpdateDoctorProfileRequest;
import com.bookinghealth.api.dto.response.client.QualificationResponse;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorVerification;
import com.bookinghealth.api.entity.Qualification;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.mapper.DoctorMapper;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.DoctorVerificationRepository;
import com.bookinghealth.api.repository.QualificationRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DoctorService {

  DoctorRepository doctorRepository;
  DoctorMapper doctorMapper;
  DoctorVerificationRepository doctorVerificationRepository;
  UserRepository userRepository;
  WorkScheduleService workScheduleService;
  com.bookinghealth.api.repository.DoctorReviewRepository doctorReviewRepository;
  QualificationRepository qualificationRepository;
  com.bookinghealth.api.repository.RoleRepository roleRepository;
  NotificationService notificationService;

  /** Hồ sơ đăng ký bác sĩ của user đang đăng nhập (kèm trạng thái + lý do từ chối). */
  @Transactional(readOnly = true)
  public MyDoctorApplicationResponse getMyApplication() {
    var context = SecurityContextHolder.getContext();
    String username = context.getAuthentication().getName();
    User user =
        userRepository
            .findByPhone(username)
            .or(() -> userRepository.findByEmail(username))
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Doctor doctor = user.getDoctor();
    if (doctor == null) {
      return null; // chưa từng nộp hồ sơ bác sĩ
    }

    String reason =
        doctorVerificationRepository
            .findByDoctor(doctor)
            .map(DoctorVerification::getReason)
            .orElse(null);

    java.util.List<Long> specialtyIds = new java.util.ArrayList<>();
    java.util.List<String> specialtyNames = new java.util.ArrayList<>();
    if (doctor.getSpecialties() != null) {
      doctor
          .getSpecialties()
          .forEach(
              s -> {
                specialtyIds.add(s.getId());
                specialtyNames.add(s.getSpecialtyName());
              });
    }

    return MyDoctorApplicationResponse.builder()
        .doctorId(doctor.getId())
        .status(doctor.getStatus())
        .rejectReason(reason)
        .name(user.getName())
        .phone(user.getPhone())
        .email(user.getEmail())
        .avatar(user.getAvatar())
        .practiceLicenseNumber(doctor.getPracticeLicenseNumber())
        .practiceStartDate(doctor.getPracticeStartDate())
        .biography(doctor.getBiography())
        .practiceLicenseImage(doctor.getPracticeLicenseImage())
        .clinicId(doctor.getClinic() != null ? doctor.getClinic().getId() : null)
        .clinicName(doctor.getClinic() != null ? doctor.getClinic().getClinicName() : null)
        .specialtyIds(specialtyIds)
        .specialtyNames(specialtyNames)
        .build();
  }

  public Page<DoctorAdminResponse> getAllDoctors(
      int page, int size, String search, Integer status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

    return doctorRepository.searchDoctorsForAdmin(searchParam, status, pageable);
  }

  @Transactional
  public void updateDoctorStatus(Long doctorId, DoctorStatusUpdateRequest request) {
    Doctor doctor =
        doctorRepository
            .findById(doctorId)
            .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

    if (request.getStatus() == 1) { // 1 là VERIFIED (Đã duyệt)
      doctor.setStatus(1);
      User user = doctor.getUser();
      if (user != null) {
        user.setStatus(1); // 1 là Hoạt động
        // Cấp quyền DOCTOR cho user khi được duyệt
        roleRepository.findByRoleName(com.bookinghealth.api.constant.PredefinedRole.DOCTOR_ROLE)
            .ifPresent(user.getRoles()::add);
        userRepository.save(user);

        // Gửi thông báo
        notificationService.createNotification(
            user,
            "Hồ sơ đã được duyệt",
            "Chúc mừng! Hồ sơ Bác sĩ của bạn đã được quản trị viên phê duyệt. Bây giờ bạn có thể truy cập cổng dành cho Bác sĩ.",
            1 // Type 1: System/Account Notification
        );
      }
    } else if (request.getStatus() == 2) { // 2 là REJECTED (Từ chối)
      doctor.setStatus(2);
      if (request.getRejectReason() != null && !request.getRejectReason().trim().isEmpty()) {
        log.info(
            "Hồ sơ bác sĩ ID {} bị từ chối với lý do: {}", doctorId, request.getRejectReason());
      }
      User user = doctor.getUser();
      if (user != null) {
        String reason = (request.getRejectReason() != null && !request.getRejectReason().trim().isEmpty()) 
            ? request.getRejectReason() : "Hồ sơ chưa đạt yêu cầu";
        notificationService.createNotification(
            user,
            "Hồ sơ bị từ chối",
            "Hồ sơ đăng ký Bác sĩ của bạn đã bị từ chối với lý do: " + reason + ". Vui lòng cập nhật lại thông tin.",
            2 // Type 2: Alert/Warning
        );
      }
    } else if (request.getStatus() == 3) { // 3 là LOCKED (Khóa)
      doctor.setStatus(3); // Hoặc bạn có thể giữ nguyên status hiện tại nếu chỉ muốn khóa User
      User user = doctor.getUser();
      if (user != null) {
        user.setStatus(0); // 0 là Khóa (Ngừng hoạt động)
      }
    }

    doctorRepository.save(doctor);

    // Retrieve currently logged-in Admin
    User admin = null;
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      String adminIdentifier = authentication.getName();
      admin = userRepository.findByEmail(adminIdentifier).orElse(null);
      if (admin == null) {
        admin = userRepository.findByPhone(adminIdentifier).orElse(null);
      }
    }

    // Update DoctorVerification request
    DoctorVerification verification =
        doctorVerificationRepository
            .findByDoctor(doctor)
            .orElseGet(() -> DoctorVerification.builder().doctor(doctor).build());

    verification.setStatus(request.getStatus());
    verification.setReason(request.getStatus() == 2 ? request.getRejectReason() : null);
    verification.setAdmin(admin);

    doctorVerificationRepository.save(verification);
  }

  public Page<DoctorResponse> getDoctorsForClient(
      int page, int size, Long specialtyId, Long clinicId, String search) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    Page<Doctor> doctors =
        doctorRepository.searchDoctorsForClient(specialtyId, clinicId, searchParam, pageable);
    return doctors.map(doctorMapper::toDoctorResponse);
  }

  public DoctorResponse getDoctorByIdForClient(Long id) {
    Doctor doctor =
        doctorRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
    if (doctor.getStatus() != 1) {
      throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
    }
    return doctorMapper.toDoctorResponse(doctor);
  }

  public java.util.List<WorkScheduleResponse> getWorkSchedules(Long doctorId, String dateStr) {
    return workScheduleService.getWorkSchedulesForClient(doctorId, dateStr);
  }

  public Page<com.bookinghealth.api.dto.response.admin.WorkScheduleAdminResponse>
      getWorkSchedulesForAdmin(String dateStr, Long clinicId, Long doctorId, Pageable pageable) {
    return workScheduleService.getWorkSchedulesForAdmin(dateStr, clinicId, doctorId, pageable);
  }

  public Page<com.bookinghealth.api.dto.response.client.DoctorReviewResponse> getReviewsForDoctor(
      Long doctorId, Pageable pageable) {
    if (!doctorRepository.existsById(doctorId)) {
      throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
    }
    return doctorReviewRepository
        .findByDoctorId(doctorId, pageable)
        .map(doctorMapper::toDoctorReviewResponse);
  }

  // ─── Cập nhật Hồ sơ Bác sĩ (Biograpy) ───
  @Transactional
  public DoctorResponse updateDoctorProfile(Long doctorId, UpdateDoctorProfileRequest request) {
    Doctor doctor = doctorRepository.findById(doctorId)
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

    if (request.getBiography() != null) {
      doctor.setBiography(request.getBiography());
    }

    Doctor saved = doctorRepository.save(doctor);
    return doctorMapper.toDoctorResponse(saved);
  }

  // ─── Đăng ký văn bằng/chứng chỉ (Client - Doctor) ───
  @Transactional
  public QualificationResponse addQualification(Long doctorId, QualificationRequest request) {
    Doctor doctor = doctorRepository.findById(doctorId)
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

    LocalDateTime issueDate = null;
    if (request.getIssueDate() != null && !request.getIssueDate().isEmpty()) {
      try {
        // Assume format yyyy-MM-dd
        issueDate = java.time.LocalDate.parse(request.getIssueDate()).atStartOfDay();
      } catch (DateTimeParseException e) {
        log.error("Invalid issue date format: {}", request.getIssueDate());
      }
    }

    Qualification qualification = Qualification.builder()
        .doctor(doctor)
        .qualificationName(request.getDegree())
        .issueDate(issueDate)
        .attachmentUrl(request.getAttachmentUrl())
        .status(0) // 0: Pending
        .build();

    Qualification saved = qualificationRepository.save(qualification);
    return doctorMapper.toQualificationResponse(saved);
  }

  // ─── Duyệt/Từ chối chứng chỉ (Admin) ───
  @Transactional
  public QualificationResponse approveQualification(Long id, Integer status) {
    Qualification qualification = qualificationRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND)); // Or create QUALIFICATION_NOT_FOUND

    // 1 - Approved, 2 - Rejected
    qualification.setStatus(status);
    Qualification saved = qualificationRepository.save(qualification);
    return doctorMapper.toQualificationResponse(saved);
  }
}
