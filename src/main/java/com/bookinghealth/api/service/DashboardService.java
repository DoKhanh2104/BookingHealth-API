package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.response.admin.dashboard.*;
import com.bookinghealth.api.entity.Doctor;
import com.bookinghealth.api.entity.DoctorReview;
import com.bookinghealth.api.repository.AppointmentRepository;
import com.bookinghealth.api.repository.DoctorRepository;
import com.bookinghealth.api.repository.DoctorReviewRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

  AppointmentRepository appointmentRepository;
  DoctorRepository doctorRepository;
  DoctorReviewRepository doctorReviewRepository;
  UserRepository userRepository;

  static final int STATUS_PENDING = 0;
  static final int STATUS_COMPLETED = 2;
  static final int STATUS_CANCELLED = 3;

  public DashboardSummaryResponse getSummary(String period) {
    LocalDate[] range = getDateRange(period);
    LocalDate from = range[0], to = range[1];

    long totalBookings = appointmentRepository.count();
    double totalRevenue = appointmentRepository.sumRevenueAllTime();
    long totalVerifiedDoctors = doctorRepository.countByStatus(1);
    long totalUsers = userRepository.countAllPatients();

    return DashboardSummaryResponse.builder()
        .totalBookings(totalBookings)
        .newDoctors(totalVerifiedDoctors) // Reuse the field name to not break frontend
        .newUsers(totalUsers) // Already fetches total users
        .totalRevenue(totalRevenue)
        .build();
  }

  public RevenueChartResponse getRevenueChart(String filter) {
    return switch (filter) {
      case "month" -> buildMonthlyRevenue();
      case "year" -> buildYearlyRevenue();
      default -> buildWeeklyRevenue(); // "week"
    };
  }

  private RevenueChartResponse buildWeeklyRevenue() {
    LocalDate today = LocalDate.now();
    LocalDate from = today.with(DayOfWeek.MONDAY);
    LocalDate to = today.with(DayOfWeek.SUNDAY);

    Map<LocalDate, Double> revenueMap = new LinkedHashMap<>();
    for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
      revenueMap.put(d, 0.0);
    }

    List<Object[]> rows = appointmentRepository.revenueGroupByDay(from, to);
    for (Object[] row : rows) {
      LocalDate date = (LocalDate) row[0];
      double amount = ((Number) row[1]).doubleValue();
      revenueMap.put(date, amount);
    }

    List<String> labels = new ArrayList<>();
    List<Double> data = new ArrayList<>();
    String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
    int idx = 0;
    for (Map.Entry<LocalDate, Double> entry : revenueMap.entrySet()) {
      labels.add(dayNames[idx++]);
      data.add(entry.getValue());
    }

    return RevenueChartResponse.builder().labels(labels).data(data).build();
  }

  private RevenueChartResponse buildMonthlyRevenue() {
    LocalDate today = LocalDate.now();
    LocalDate from = today.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate to = today.with(TemporalAdjusters.lastDayOfMonth());

    // Group by week number inside month
    Map<Integer, Double> weekMap = new TreeMap<>();
    for (int w = 1; w <= 5; w++) weekMap.put(w, 0.0);

    List<Object[]> rows = appointmentRepository.revenueGroupByDay(from, to);
    for (Object[] row : rows) {
      LocalDate date = (LocalDate) row[0];
      double amount = ((Number) row[1]).doubleValue();
      int weekNum = ((date.getDayOfMonth() - 1) / 7) + 1;
      weekMap.merge(weekNum, amount, Double::sum);
    }

    // Trim empty trailing weeks
    List<String> labels = new ArrayList<>();
    List<Double> data = new ArrayList<>();
    weekMap.forEach(
        (w, v) -> {
          labels.add("Tuần " + w);
          data.add(v);
        });
    // Remove trailing zero weeks beyond actual days in month
    int maxWeek = ((to.getDayOfMonth() - 1) / 7) + 1;
    while (labels.size() > maxWeek) {
      labels.remove(labels.size() - 1);
      data.remove(data.size() - 1);
    }

    return RevenueChartResponse.builder().labels(labels).data(data).build();
  }

  private RevenueChartResponse buildYearlyRevenue() {
    LocalDate from = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    LocalDate to = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

    Map<String, Double> monthMap = new LinkedHashMap<>();
    for (int m = 1; m <= 12; m++) {
      monthMap.put(String.format("%d-%02d", from.getYear(), m), 0.0);
    }

    List<Object[]> rows = appointmentRepository.revenueGroupByMonth(from, to);
    for (Object[] row : rows) {
      String ym = (String) row[0];
      double amount = ((Number) row[1]).doubleValue();
      monthMap.put(ym, amount);
    }

    String[] monthLabels = {
      "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"
    };
    List<String> labels = new ArrayList<>(Arrays.asList(monthLabels));
    List<Double> data = new ArrayList<>(monthMap.values());

    return RevenueChartResponse.builder().labels(labels).data(data).build();
  }

  public BookingStatusResponse getBookingStatus(String period) {
    long pending = appointmentRepository.countByStatus(STATUS_PENDING);
    long completed = appointmentRepository.countByStatus(STATUS_COMPLETED);
    long cancelled = appointmentRepository.countByStatus(STATUS_CANCELLED);

    return BookingStatusResponse.builder()
        .pending(pending)
        .completed(completed)
        .cancelled(cancelled)
        .build();
  }

  public List<SpecialtyDistributionItem> getSpecialtyDistribution(String period) {
    List<Object[]> rows = appointmentRepository.bookingsBySpecialtyAllTime();

    return rows.stream()
        .map(
            row ->
                SpecialtyDistributionItem.builder()
                    .category((String) row[0])
                    .value(((Number) row[1]).longValue())
                    .build())
        .collect(Collectors.toList());
  }

  public List<TopDoctorResponse> getTopDoctors(String period) {
    // Lấy top 5
    List<Object[]> rows = appointmentRepository.topDoctorsByBookingsAllTime(PageRequest.of(0, 5));

    List<TopDoctorResponse> result = new ArrayList<>();
    for (Object[] row : rows) {
      Long doctorId = ((Number) row[0]).longValue();
      long bookings = ((Number) row[1]).longValue();
      double avgRating = doctorReviewRepository.avgRatingByDoctorId(doctorId);

      doctorRepository
          .findById(doctorId)
          .ifPresent(
              doc -> {
                String doctorName = doc.getUser() != null ? doc.getUser().getName() : "Bác sĩ";
                String specName =
                    doc.getSpecialties() != null && !doc.getSpecialties().isEmpty()
                        ? doc.getSpecialties().iterator().next().getSpecialtyName()
                        : "";

                result.add(
                    TopDoctorResponse.builder()
                        .name(doctorName)
                        .spec(specName)
                        .rating(Math.round(avgRating * 10.0) / 10.0)
                        .bookings(bookings)
                        .avatar(doctorName.isEmpty() ? "?" : String.valueOf(doctorName.charAt(0)))
                        .build());
              });
    }
    return result;
  }

  public List<RecentFeedbackResponse> getRecentFeedbacks() {
    List<DoctorReview> reviews =
        doctorReviewRepository.findRecentLowRatingFeedbacks(2, PageRequest.of(0, 5));

    return reviews.stream()
        .map(
            r -> {
              String patientName = r.getUser() != null ? r.getUser().getName() : "Bệnh nhân";
              String doctorName =
                  (r.getDoctor() != null && r.getDoctor().getUser() != null)
                      ? r.getDoctor().getUser().getName()
                      : "Bác sĩ";

              return RecentFeedbackResponse.builder()
                  .id(r.getId())
                  .patientName(patientName)
                  .doctorName(doctorName)
                  .rating(r.getRating() != null ? r.getRating() : 0)
                  .comment(r.getComment() != null ? r.getComment() : "")
                  .date(formatRelativeTime(r.getId()))
                  .build();
            })
        .collect(Collectors.toList());
  }

  public List<PendingDoctorResponse> getPendingDoctors() {
    List<Doctor> pendingDoctors = doctorRepository.findPendingDoctors();

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    return pendingDoctors.stream()
        .map(
            doc -> {
              String name = doc.getUser() != null ? doc.getUser().getName() : "Bác sĩ";
              String spec =
                  doc.getSpecialties() != null && !doc.getSpecialties().isEmpty()
                      ? doc.getSpecialties().iterator().next().getSpecialtyName()
                      : "";
              String date =
                  doc.getPracticeStartDate() != null ? doc.getPracticeStartDate().format(fmt) : "";
              String certStatus =
                  doc.getPracticeLicenseImage() != null && !doc.getPracticeLicenseImage().isBlank()
                      ? "Đã tải lên"
                      : "Chưa tải lên";

              return PendingDoctorResponse.builder()
                  .id(doc.getId())
                  .name(name)
                  .spec(spec)
                  .date(date)
                  .certStatus(certStatus)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private LocalDate[] getDateRange(String period) {
    LocalDate today = LocalDate.now();
    return switch (period) {
      case "week" -> new LocalDate[] {today.with(DayOfWeek.MONDAY), today.with(DayOfWeek.SUNDAY)};
      case "month" ->
          new LocalDate[] {
            today.with(TemporalAdjusters.firstDayOfMonth()),
            today.with(TemporalAdjusters.lastDayOfMonth())
          };
      default -> new LocalDate[] {today, today}; // "day"
    };
  }

  private String formatRelativeTime(Long id) {
    return "Gần đây";
  }
}
