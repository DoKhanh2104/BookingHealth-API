package com.bookinghealth.api.dto.request.client;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookAppointmentRequest {

    @NotNull(message = "Mã bác sĩ không được để trống")
    Long doctorId;

    @NotNull(message = "Mã ca khám không được để trống")
    Long appointmentSlotId;

    @NotNull(message = "Ngày khám dự kiến không được để trống")
    LocalDate expectedExaminationDate;

    String description;
}
