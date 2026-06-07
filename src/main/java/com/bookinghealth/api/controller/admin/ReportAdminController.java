package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.FinancialReportResponse;
import com.bookinghealth.api.dto.response.admin.PerformanceReportResponse;
import com.bookinghealth.api.dto.response.admin.SatisfactionReportResponse;
import com.bookinghealth.api.service.ReportService;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/reports")
public class ReportAdminController {

  ReportService reportService;

  @GetMapping("/financial")
  public ApiResponse<List<FinancialReportResponse>> getFinancialReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {
    return ApiResponse.<List<FinancialReportResponse>>builder()
        .result(reportService.getFinancialReport(fromDate, toDate))
        .build();
  }

  @GetMapping("/performance")
  public ApiResponse<List<PerformanceReportResponse>> getPerformanceReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {
    return ApiResponse.<List<PerformanceReportResponse>>builder()
        .result(reportService.getPerformanceReport(fromDate, toDate))
        .build();
  }

  @GetMapping("/satisfaction")
  public ApiResponse<List<SatisfactionReportResponse>> getSatisfactionReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {
    return ApiResponse.<List<SatisfactionReportResponse>>builder()
        .result(reportService.getSatisfactionReport(fromDate, toDate))
        .build();
  }
}
