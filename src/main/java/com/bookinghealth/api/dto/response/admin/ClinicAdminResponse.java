package com.bookinghealth.api.dto.response.admin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClinicAdminResponse {
    Long id;
    String clinicName;
    String address;
    Double longitude;
    Double latitude;
    Integer soLuongBacSi;
}
