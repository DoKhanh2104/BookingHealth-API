package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.request.client.UpdateProfileRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.dto.response.client.AvatarUploadResponse;
import com.bookinghealth.api.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserClientController {
  UserService userService;

  @GetMapping("/me")
  public ApiResponse<UserResponse> getMyProfile() {
    return ApiResponse.<UserResponse>builder().result(userService.getMyProfile()).build();
  }

  @PutMapping("/me")
  public ApiResponse<UserResponse> updateMyProfile(
      @Valid @RequestBody UpdateProfileRequest request) {
    return ApiResponse.<UserResponse>builder().result(userService.updateMyProfile(request)).build();
  }

  @PostMapping("/me/avatar")
  public ApiResponse<AvatarUploadResponse> uploadMyAvatar(
      @RequestParam("file") MultipartFile file) {
    String avatarUrl = userService.uploadMyAvatar(file);
    return ApiResponse.<AvatarUploadResponse>builder()
        .result(new AvatarUploadResponse(avatarUrl))
        .build();
  }
}
