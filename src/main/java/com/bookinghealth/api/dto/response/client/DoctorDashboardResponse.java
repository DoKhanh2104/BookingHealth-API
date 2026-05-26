package com.bookinghealth.api.dto.response.client;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorDashboardResponse {
  Integer todayAppointmentsCount;
  Integer todayPendingCount;
  Integer todayCompletedCount;
  Integer totalPatientsCount;
  Double averageRating;
  Integer reviewCount;
  Double monthlyRevenue;
  List<AppointmentResponse> todayFeaturedAppointments;
}
