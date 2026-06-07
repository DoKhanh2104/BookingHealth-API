package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.projection.PerformanceReportProjection;
import com.bookinghealth.api.dto.projection.SatisfactionReportProjection;
import com.bookinghealth.api.dto.response.admin.FinancialReportResponse;
import com.bookinghealth.api.dto.response.admin.PerformanceReportResponse;
import com.bookinghealth.api.dto.response.admin.SatisfactionReportResponse;
import com.bookinghealth.api.repository.AppointmentRepository;
import com.bookinghealth.api.repository.DoctorReviewRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {

  AppointmentRepository appointmentRepository;
  DoctorReviewRepository doctorReviewRepository;

  public List<FinancialReportResponse> getFinancialReport(LocalDate fromDate, LocalDate toDate) {
    List<Object[]> results = appointmentRepository.getFinancialReport(fromDate, toDate);
    return results.stream()
        .map(
            row -> {
              Long appointmentId = ((Number) row[0]).longValue();
              String patientName = (String) row[1];
              String doctorName = (String) row[2];
              Double amount = ((Number) row[3]).doubleValue();
              String paymentMethod = (String) row[4];
              LocalDate paymentDate = (LocalDate) row[5];
              LocalDateTime paymentTime = paymentDate != null ? paymentDate.atStartOfDay() : null;

              return FinancialReportResponse.builder()
                  .appointmentId(appointmentId)
                  .patientName(patientName)
                  .doctorName(doctorName)
                  .amount(amount)
                  .paymentMethod(paymentMethod)
                  .paymentTime(paymentTime)
                  .build();
            })
        .collect(Collectors.toList());
  }

  public List<PerformanceReportResponse> getPerformanceReport(
      LocalDate fromDate, LocalDate toDate) {
    List<PerformanceReportProjection> results =
        appointmentRepository.getPerformanceReport(fromDate, toDate);
    return results.stream()
        .map(
            p -> {
              double total = p.getTotal() != null ? p.getTotal() : 0;
              double cancelled = p.getCancelled() != null ? p.getCancelled() : 0;
              double cancelRate = total > 0 ? (cancelled / total) * 100.0 : 0.0;
              // Round to 1 decimal place
              cancelRate = Math.round(cancelRate * 10.0) / 10.0;

              return PerformanceReportResponse.builder()
                  .id(p.getDoctorId())
                  .doctorOrSpecialtyName(p.getDoctorName())
                  .total(p.getTotal())
                  .completed(p.getCompleted())
                  .cancelled(p.getCancelled())
                  .cancelRate(cancelRate)
                  .build();
            })
        .collect(Collectors.toList());
  }

  public List<SatisfactionReportResponse> getSatisfactionReport(
      LocalDate fromDate, LocalDate toDate) {
    List<SatisfactionReportProjection> results =
        doctorReviewRepository.getSatisfactionReport(fromDate, toDate);
    return results.stream()
        .map(
            p ->
                SatisfactionReportResponse.builder()
                    .id(p.getDoctorId())
                    .doctorName(p.getDoctorName())
                    .specialtyName(p.getSpecialtyName())
                    .totalReviews(p.getTotalReviews())
                    .averageRating(
                        p.getAverageRating() != null
                            ? Math.round(p.getAverageRating() * 10.0) / 10.0
                            : 0.0)
                    .negativeReviews(p.getNegativeReviews() != null ? p.getNegativeReviews() : 0L)
                    .build())
        .collect(Collectors.toList());
  }
}
