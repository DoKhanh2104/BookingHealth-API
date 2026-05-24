package com.bookinghealth.api.dto.response.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvatarUploadResponse {
    String avatarUrl;
}
