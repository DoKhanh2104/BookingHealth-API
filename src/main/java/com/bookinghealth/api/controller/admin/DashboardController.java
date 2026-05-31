package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.dashboard.*;
import com.bookinghealth.api.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    DashboardService dashboardService;

    /**
     * GET /admin/dashboard/summary?period=day|week|month
     * Tổng quan số liệu thống kê (booking, bác sĩ, người dùng, doanh thu).
     */
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @RequestParam(defaultValue = "day") String period) {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(dashboardService.getSummary(period))
                .build();
    }

    /**
     * GET /admin/dashboard/revenue?filter=week|month|year
     * Dữ liệu biểu đồ doanh thu.
     */
    @GetMapping("/revenue")
    public ApiResponse<RevenueChartResponse> getRevenueChart(
            @RequestParam(defaultValue = "week") String filter) {
        return ApiResponse.<RevenueChartResponse>builder()
                .result(dashboardService.getRevenueChart(filter))
                .build();
    }

    /**
     * GET /admin/dashboard/booking-status?period=day|week|month
     * Số lượng lịch hẹn theo trạng thái (pending / completed / cancelled).
     */
    @GetMapping("/booking-status")
    public ApiResponse<BookingStatusResponse> getBookingStatus(
            @RequestParam(defaultValue = "day") String period) {
        return ApiResponse.<BookingStatusResponse>builder()
                .result(dashboardService.getBookingStatus(period))
                .build();
    }

    /**
     * GET /admin/dashboard/specialty-distribution?period=day|week|month
     * Phân bố số lượt đặt theo chuyên khoa.
     */
    @GetMapping("/specialty-distribution")
    public ApiResponse<List<SpecialtyDistributionItem>> getSpecialtyDistribution(
            @RequestParam(defaultValue = "day") String period) {
        return ApiResponse.<List<SpecialtyDistributionItem>>builder()
                .result(dashboardService.getSpecialtyDistribution(period))
                .build();
    }

    /**
     * GET /admin/dashboard/top-doctors?period=day|week|month
     * Top 5 bác sĩ có nhiều lượt đặt lịch nhất (completed).
     */
    @GetMapping("/top-doctors")
    public ApiResponse<List<TopDoctorResponse>> getTopDoctors(
            @RequestParam(defaultValue = "day") String period) {
        return ApiResponse.<List<TopDoctorResponse>>builder()
                .result(dashboardService.getTopDoctors(period))
                .build();
    }

    /**
     * GET /admin/dashboard/recent-feedbacks
     * Feedback có rating thấp (≤ 2 sao) gần đây nhất.
     */
    @GetMapping("/recent-feedbacks")
    public ApiResponse<List<RecentFeedbackResponse>> getRecentFeedbacks() {
        return ApiResponse.<List<RecentFeedbackResponse>>builder()
                .result(dashboardService.getRecentFeedbacks())
                .build();
    }

    /**
     * GET /admin/dashboard/pending-doctors
     * Danh sách bác sĩ chưa được phê duyệt (status = 0).
     */
    @GetMapping("/pending-doctors")
    public ApiResponse<List<PendingDoctorResponse>> getPendingDoctors() {
        return ApiResponse.<List<PendingDoctorResponse>>builder()
                .result(dashboardService.getPendingDoctors())
                .build();
    }
}
