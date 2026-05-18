package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.response.admin.DoctorAdminResponse;
import com.bookinghealth.api.repository.DoctorRepository;
import lombok.AccessLevel;
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
    }

    doctorRepository.save(doctor);
  }
}
