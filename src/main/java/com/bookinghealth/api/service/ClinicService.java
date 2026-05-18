package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.admin.ClinicCreateRequest;
import com.bookinghealth.api.dto.request.admin.GeocodingRequest;
import com.bookinghealth.api.dto.request.admin.HealthDepartmentRequest;
import com.bookinghealth.api.dto.response.admin.GeocodingResponse;
import com.bookinghealth.api.dto.response.admin.ClinicAdminResponse;
import com.bookinghealth.api.entity.Clinic;
import com.bookinghealth.api.repository.ClinicRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClinicService {

  static final Object GEOCODE_LOCK = new Object();
  static volatile long lastGeocodeAtMs = 0;
  static final long GEOCODE_MIN_INTERVAL_MS = 1100;
  static final int GEOCODE_MAX_RETRIES = 3;
  static final long GEOCODE_429_WAIT_MS = 30_000;

  ClinicRepository clinicRepository;
  RestTemplate restTemplate;
  ObjectMapper objectMapper;

  @lombok.experimental.NonFinal
  @org.springframework.beans.factory.annotation.Value("${locationiq.api.key}")
  String locationIqKey;

  public GeocodingResponse getGeocoding(GeocodingRequest request) {
    return getGeocodingWithRetry(request, GEOCODE_MAX_RETRIES);
  }

  private GeocodingResponse getGeocodingWithRetry(GeocodingRequest request, int attemptsLeft) {
    try {
      waitForGeocodeSlot();

      String query = request.getAddress();
      if (query == null || query.isBlank()) {
        return null;
      }
      if (!query.toLowerCase().contains("đà nẵng")) {
        query = query + ", Đà Nẵng";
      }

      UriComponentsBuilder builder =
          UriComponentsBuilder.fromUriString("https://us1.locationiq.com/v1/search");
      builder.queryParam("key", locationIqKey);
      builder.queryParam("format", "json");
      builder.queryParam("q", query);
      builder.queryParam("limit", 1);

      HttpHeaders headers = new HttpHeaders();
      headers.set("User-Agent", "BookingHealthApplication/1.0");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response =
          restTemplate.exchange(
              builder.build().toUriString(), HttpMethod.GET, entity, String.class);

      JsonNode root = objectMapper.readTree(response.getBody());
      if (root.isArray() && !root.isEmpty()) {
        JsonNode firstResult = root.get(0);
        Double lat = Double.parseDouble(firstResult.get("lat").asText());
        Double lon = Double.parseDouble(firstResult.get("lon").asText());
        return new GeocodingResponse(lon, lat);
      }
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().value() == 429 && attemptsLeft > 1) {
        log.warn("LocationIQ 429 — chờ {}s rồi thử lại...", GEOCODE_429_WAIT_MS / 1000);
        sleepQuietly(GEOCODE_429_WAIT_MS);
        return getGeocodingWithRetry(request, attemptsLeft - 1);
      }
      log.error("Geocoding HTTP {}: {}", e.getStatusCode().value(), e.getMessage());
    } catch (Exception e) {
      log.error("Lỗi gọi API LocationIQ: {}", e.getMessage());
    }
    return null;
  }

  static void waitForGeocodeSlot() {
    synchronized (GEOCODE_LOCK) {
      long elapsed = System.currentTimeMillis() - lastGeocodeAtMs;
      long wait = GEOCODE_MIN_INTERVAL_MS - elapsed;
      if (wait > 0) {
        sleepQuietly(wait);
      }
      lastGeocodeAtMs = System.currentTimeMillis();
    }
  }

  static void sleepQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public boolean clinicExistsFor(HealthDepartmentRequest request) {
    String clinicName = request.getMedicalFacilityName();
    if (clinicName == null || clinicName.isBlank()) {
      return true;
    }
    return clinicRepository.existsByClinicNameAndAddress(clinicName, buildFullAddress(request));
  }

  public String buildFullAddress(HealthDepartmentRequest request) {
    StringBuilder sb = new StringBuilder();
    if (request.getAddress() != null && !request.getAddress().isBlank()) {
      sb.append(request.getAddress().trim());
    }
    if (request.getWard() != null && !request.getWard().isBlank()) {
      if (!sb.isEmpty()) sb.append(", ");
      sb.append(request.getWard().trim());
    }
    if (request.getDistrict() != null && !request.getDistrict().isBlank()) {
      if (!sb.isEmpty()) sb.append(", ");
      sb.append(request.getDistrict().trim());
    }
    if (!sb.isEmpty()) sb.append(", ");
    sb.append("Đà Nẵng");
    return sb.toString();
  }

  /**
   * @return true nếu đã insert PHONG_KHAM
   */
  @Transactional
  public boolean createClinicFromHealthDepartment(HealthDepartmentRequest request) {
    String clinicName = request.getMedicalFacilityName();
    String fullAddress = buildFullAddress(request);

    if (clinicName == null || clinicName.isBlank() || fullAddress.isBlank()) {
      log.warn("Bỏ qua PHONG_KHAM [{}]: thiếu tên hoặc địa chỉ", request.getId());
      return false;
    }

    if (clinicRepository.existsByClinicNameAndAddress(clinicName, fullAddress)) {
      return false;
    }

    GeocodingResponse geo = getGeocoding(new GeocodingRequest(fullAddress));
    if (geo == null || geo.getLatitude() == null || geo.getLongitude() == null) {
      log.warn("Không lấy được tọa độ cho [{}] — {}", request.getId(), clinicName);
      return false;
    }

    return saveClinicEntity(clinicName, fullAddress, geo.getLongitude(), geo.getLatitude());
  }

  @Transactional
  public boolean saveClinic(ClinicCreateRequest request) {
    if (clinicRepository.existsByClinicNameAndAddress(request.getName(), request.getAddress())) {
      return false;
    }
    return saveClinicEntity(
        request.getName(), request.getAddress(), request.getLongitude(), request.getLatitude());
  }

  private boolean saveClinicEntity(
      String clinicName, String fullAddress, Double longitude, Double latitude) {
    Clinic clinic =
        Clinic.builder()
            .clinicName(clinicName)
            .address(fullAddress)
            .longitude(longitude)
            .latitude(latitude)
            .build();
    clinicRepository.save(clinic);
    log.info("Đã lưu PHONG_KHAM: {}", clinicName);
    return true;
  }

  public Page<ClinicAdminResponse> getAllClinics(Pageable pageable) {
    return clinicRepository.findAll(pageable).map(clinic -> ClinicAdminResponse.builder()
        .id(clinic.getId())
        .clinicName(clinic.getClinicName())
        .address(clinic.getAddress())
        .longitude(clinic.getLongitude())
        .latitude(clinic.getLatitude())
        .soLuongBacSi(clinic.getDoctors() != null ? clinic.getDoctors().size() : 0)
        .build());
  }
}
