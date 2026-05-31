package com.bookinghealth.api.dto.projection;

public interface PerformanceReportProjection {
  Long getDoctorId();
  String getDoctorName();
  Long getTotal();
  Long getCompleted();
  Long getCancelled();
}
