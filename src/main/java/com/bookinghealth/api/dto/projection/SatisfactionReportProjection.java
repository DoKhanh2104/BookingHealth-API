package com.bookinghealth.api.dto.projection;

public interface SatisfactionReportProjection {
  Long getDoctorId();
  String getDoctorName();
  String getSpecialtyName();
  Long getTotalReviews();
  Double getAverageRating();
  Long getNegativeReviews();
}
