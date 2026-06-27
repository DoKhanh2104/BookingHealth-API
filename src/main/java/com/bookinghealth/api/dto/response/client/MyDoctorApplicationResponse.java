package com.bookinghealth.api.dto.response.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Hồ sơ đăng ký bác sĩ của chính người dùng đang đăng nhập — kèm trạng thái duyệt
 * và lý do từ chối (nếu có) + dữ liệu để prefill khi nộp lại.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyDoctorApplicationResponse {
  Long doctorId;
  Integer status; // 0=PENDING, 1=VERIFIED, 2=REJECTED, 3=LOCKED
  String rejectReason;

  String name;
  String phone;
  String email;
  String avatar;

  String practiceLicenseNumber;

  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate practiceStartDate;

  String biography;
  String practiceLicenseImage;

  Long clinicId;
  String clinicName;

  List<Long> specialtyIds;
  List<String> specialtyNames;
}
