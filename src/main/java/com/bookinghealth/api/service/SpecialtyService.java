package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.admin.SpecialtyRequest;
import com.bookinghealth.api.dto.response.admin.SpecialtyResponse;
import com.bookinghealth.api.entity.Specialty;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.mapper.SpecialtyMapper;
import com.bookinghealth.api.repository.SpecialtyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpecialtyService {
  SpecialtyRepository specialtyRepository;
  SpecialtyMapper specialtyMapper;

  public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
    if (specialtyRepository.existsBySpecialtyName(request.getSpecialtyName()))
      throw new AppException(ErrorCode.SPECIALTY_EXISTED);

    Specialty specialty = specialtyMapper.toSpecialty(request);

    specialty = specialtyRepository.save(specialty);

    return specialtyMapper.toSpecialtyResponse(specialty);
  }

  public Page<SpecialtyResponse> getAllSpecialties(Pageable pageable) {
    return specialtyRepository.findAll(pageable).map(specialtyMapper::toSpecialtyResponse);
  }

  public SpecialtyResponse updateSpecialty(Long id, SpecialtyRequest request) {
    Specialty specialty =
        specialtyRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

    specialtyMapper.updateSpecialtyFromRequest(specialty, request);

    return specialtyMapper.toSpecialtyResponse(specialtyRepository.save(specialty));
  }

  public void deleteSpecialty(Long id) {
    if (!specialtyRepository.existsById(id)) {
      throw new AppException(ErrorCode.SPECIALTY_NOT_FOUND);
    }
    specialtyRepository.deleteById(id);
  }
}
