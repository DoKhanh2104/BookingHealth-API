package com.bookinghealth.api.dto.request.admin;

import lombok.Data;

/** Body khi admin từ chối đơn nghỉ phép — kèm lý do (tùy chọn). */
@Data
public class DayOffRejectRequest {
  String reason;
}
