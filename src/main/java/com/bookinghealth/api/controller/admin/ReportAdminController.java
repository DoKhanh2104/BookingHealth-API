package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.FinancialReportResponse;
import com.bookinghealth.api.dto.response.admin.PerformanceReportResponse;
import com.bookinghealth.api.dto.response.admin.SatisfactionReportResponse;
import com.bookinghealth.api.service.ReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/reports")
public class ReportAdminController {

  ReportService reportService;

  @GetMapping("/financial")
  public ApiResponse<List<FinancialReportResponse>> getFinancialReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @RequestParam(required = false) Long specialtyId,
      @RequestParam(required = false) Long clinicId) {
    return ApiResponse.<List<FinancialReportResponse>>builder()
        .result(reportService.getFinancialReport(fromDate, toDate, specialtyId, clinicId))
        .build();
  }

  @GetMapping("/performance")
  public ApiResponse<List<PerformanceReportResponse>> getPerformanceReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @RequestParam(required = false) Long specialtyId,
      @RequestParam(required = false) Long clinicId) {
    return ApiResponse.<List<PerformanceReportResponse>>builder()
        .result(reportService.getPerformanceReport(fromDate, toDate, specialtyId, clinicId))
        .build();
  }

  @GetMapping("/satisfaction")
  public ApiResponse<List<SatisfactionReportResponse>> getSatisfactionReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @RequestParam(required = false) Long specialtyId,
      @RequestParam(required = false) Long clinicId) {
    return ApiResponse.<List<SatisfactionReportResponse>>builder()
        .result(reportService.getSatisfactionReport(fromDate, toDate, specialtyId, clinicId))
        .build();
  }
}
