package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.ScreenLogRequest;
import com.bookinghealth.api.dto.response.ScreenLogResponse;
import com.bookinghealth.api.entity.ScreenLog;
import com.bookinghealth.api.entity.Specialty;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.repository.ScreenLogRepository;
import com.bookinghealth.api.repository.SpecialtyRepository;
import com.bookinghealth.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScreenLogService {

  ScreenLogRepository screenLogRepository;
  SpecialtyRepository specialtyRepository;
  UserRepository userRepository;

  @Transactional
  public ScreenLogResponse createScreenLog(ScreenLogRequest request) {
    User user = null;
    try {
      var authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null
          && authentication.isAuthenticated()
          && !authentication.getPrincipal().equals("anonymousUser")) {
        String userEmail = authentication.getName();
        user = userRepository.findByEmail(userEmail).orElse(null);
      }
    } catch (Exception e) {
      log.warn("Could not get authenticated user for screen log: {}", e.getMessage());
    }

    Specialty specialty = null;
    if (request.getSpecialtyName() != null && !request.getSpecialtyName().isBlank()) {
      specialty =
          specialtyRepository
              .findFirstBySpecialtyNameContainingIgnoreCase(request.getSpecialtyName())
              .orElse(null);
    }

    ScreenLog screenLog =
        ScreenLog.builder()
            .user(user)
            .suggestedSpecialty(specialty)
            .symptoms(request.getSymptoms())
            .build();

    screenLog = screenLogRepository.save(screenLog);

    return ScreenLogResponse.builder()
        .id(screenLog.getId())
        .userId(user != null ? user.getId() : null)
        .specialtyName(specialty != null ? specialty.getSpecialtyName() : null)
        .symptoms(screenLog.getSymptoms())
        .build();
  }
}
