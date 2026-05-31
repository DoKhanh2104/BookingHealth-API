package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.response.admin.DoctorAdminResponse;
import com.bookinghealth.api.dto.response.client.DoctorResponse;
import com.bookinghealth.api.entity.DoctorVerification;
import com.bookinghealth.api.mapper.DoctorMapper;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.DoctorVerificationRepository;
import com.bookinghealth.api.repository.UserRepository;
import com.bookinghealth.api.dto.response.client.WorkScheduleResponse;
import lombok.AccessLevel;

import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bookinghealth.api.dto.request.admin.DoctorStatusUpdateRequest;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

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


  public Page<DoctorAdminResponse> getAllDoctors(
      int page, int size, String search, Integer status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

    return doctorRepository.searchDoctorsForAdmin(searchParam, status, pageable);
  }

  @Transactional
  public void updateDoctorStatus(Long doctorId, DoctorStatusUpdateRequest request) {
    Doctor doctor = doctorRepository.findById(doctorId)
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

    if (request.getStatus() == 1) { // 1 là VERIFIED (Đã duyệt)
      doctor.setStatus(1);
      User user = doctor.getUser();
      if (user != null) {
        user.setStatus(1); // 1 là Hoạt động
      }
    } else if (request.getStatus() == 2) { // 2 là REJECTED (Từ chối)
      doctor.setStatus(2);
      if (request.getRejectReason() != null && !request.getRejectReason().trim().isEmpty()) {
        log.info("Hồ sơ bác sĩ ID {} bị từ chối với lý do: {}", doctorId, request.getRejectReason());
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
    DoctorVerification verification = doctorVerificationRepository.findByDoctor(doctor)
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
    Page<Doctor> doctors = doctorRepository.searchDoctorsForClient(specialtyId, clinicId, searchParam, pageable);
    return doctors.map(doctorMapper::toDoctorResponse);
  }

  public DoctorResponse getDoctorByIdForClient(Long id) {
    Doctor doctor = doctorRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
    if (doctor.getStatus() != 1) {
      throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
    }
    return doctorMapper.toDoctorResponse(doctor);
  }

  public java.util.List<WorkScheduleResponse> getWorkSchedules(Long doctorId, String dateStr) {
    return workScheduleService.getWorkSchedulesForClient(doctorId, dateStr);
  }

  public Page<com.bookinghealth.api.dto.response.admin.WorkScheduleAdminResponse> getWorkSchedulesForAdmin(
      String dateStr, Long clinicId, Long doctorId, Pageable pageable) {
    return workScheduleService.getWorkSchedulesForAdmin(dateStr, clinicId, doctorId, pageable);
  }

  public Page<com.bookinghealth.api.dto.response.client.DoctorReviewResponse> getReviewsForDoctor(Long doctorId, Pageable pageable) {
    if (!doctorRepository.existsById(doctorId)) {
      throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
    }
    return doctorReviewRepository.findByDoctorId(doctorId, pageable)
        .map(doctorMapper::toDoctorReviewResponse);
  }
}
