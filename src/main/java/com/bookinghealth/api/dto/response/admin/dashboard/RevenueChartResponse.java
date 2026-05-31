package com.bookinghealth.api.dto.response.admin.dashboard;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueChartResponse {

    List<String> labels;
    List<Double> data;
}
