package com.bookinghealth.api.dto.response.admin.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingDoctorResponse {

    Long id;
    String name;
    String spec;
    /** Ngày đăng ký dạng dd/MM/yyyy */
    String date;
    /** Trạng thái chứng chỉ: "Đã tải lên" / "Chưa tải lên" */
    String certStatus;
}
