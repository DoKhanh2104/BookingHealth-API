package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.admin.HealthDepartmentRequest;
import com.bookinghealth.api.entity.HealthDepartment;
import com.bookinghealth.api.repository.HealthDepartmentRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthDepartmentService {

  HealthDepartmentRepository healthDepartmentRepository;
  ClinicService clinicService;

  @Transactional
  public void saveRawData(HealthDepartmentRequest request) {
    if (healthDepartmentRepository.existsById(request.getId())) {
      log.info("-> GPHĐ [{}] đã tồn tại trong kho đối soát. SKIP!", request.getId());
      return;
    }

    HealthDepartment healthDepartment =
        HealthDepartment.builder()
            .id(request.getId())
            .issuedDate(request.getIssuedDate())
            .facilityType(request.getFacilityType())
            .organizationType(request.getOrganizationType())
            .medicalFacilityName(request.getMedicalFacilityName())
            .address(request.getAddress())
            .district(request.getDistrict())
            .ward(request.getWard())
            .phoneNumber(request.getPhoneNumber())
            .doctorName(request.getDoctorName())
            .build();

    healthDepartmentRepository.save(healthDepartment);
    log.info("-> Đã lưu SO_Y_TE: {}", request.getId());

    try {
      clinicService.createClinicFromHealthDepartment(request);
    } catch (Exception e) {
      log.warn("Không tạo được PHONG_KHAM cho [{}]: {}", request.getId(), e.getMessage());
    }
  }

  /**
   * Bổ sung phòng khám cho các GPHĐ đã có trong SO_Y_TE nhưng chưa có trên bản đồ (sau lần sync bị
   * lỗi 429).
   */
  /**
   * @param batchSize số phòng khám tối đa tạo mỗi lần gọi (tránh timeout + 429)
   * @return số phòng khám vừa tạo trong batch này
   */
  @Transactional
  public int syncMissingClinics(int batchSize) {
    List<HealthDepartment> all = healthDepartmentRepository.findAll();
    int created = 0;
    int scanned = 0;
    int maxScan = batchSize * 5;

    for (HealthDepartment hd : all) {
      if (created >= batchSize) {
        break;
      }
      if (scanned >= maxScan) {
        break;
      }
      HealthDepartmentRequest req = toRequest(hd);
      if (clinicService.clinicExistsFor(req)) {
        continue;
      }
      scanned++;
      if (clinicService.createClinicFromHealthDepartment(req)) {
        created++;
      }
    }
    log.info("Backfill batch: lưu {} PHONG_KHAM (quét {} ứng viên)", created, scanned);
    return created;
  }

  private static HealthDepartmentRequest toRequest(HealthDepartment hd) {
    return HealthDepartmentRequest.builder()
        .id(hd.getId())
        .issuedDate(hd.getIssuedDate())
        .facilityType(hd.getFacilityType())
        .organizationType(hd.getOrganizationType())
        .medicalFacilityName(hd.getMedicalFacilityName())
        .address(hd.getAddress())
        .district(hd.getDistrict())
        .ward(hd.getWard())
        .phoneNumber(hd.getPhoneNumber())
        .doctorName(hd.getDoctorName())
        .build();
  }
}
