package com.bookinghealth.api.dto.response.admin.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopDoctorResponse {

    String name;
    String spec;
    double rating;
    long bookings;
    /** Ký tự đầu của tên bác sĩ, dùng để hiển thị Avatar mặc định */
    String avatar;
}
