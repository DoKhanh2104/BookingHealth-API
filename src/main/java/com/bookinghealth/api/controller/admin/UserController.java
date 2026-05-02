package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.UserCreationRequest;
import com.bookinghealth.api.dto.request.admin.UserUpdateRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/users")
public class UserController {
  UserService userService;

  @PostMapping
  public ApiResponse<UserResponse> createUser(
      @Valid @ModelAttribute UserCreationRequest request) {
    return ApiResponse.<UserResponse>builder().result(userService.createUser(request)).build();
  }

  @PutMapping("/{id}")
  public ApiResponse<UserResponse> updateUser(
      @Valid @ModelAttribute UserUpdateRequest request, @PathVariable Long id) {
    return ApiResponse.<UserResponse>builder().result(userService.updateUser(id, request)).build();
  }

  @PatchMapping("{id}")
  public ApiResponse<String> deleteSoftUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ApiResponse.<String>builder().result("User deleted successfully").build();
  }

  @GetMapping("/{id}")
  public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
    return ApiResponse.<UserResponse>builder().result(userService.getUserById(id)).build();
  }

  @GetMapping
  public ApiResponse<Page<UserResponse>> getUsers(
      @PageableDefault(size = 5, sort = "id") Pageable pageable) {
    return ApiResponse.<Page<UserResponse>>builder().result(userService.getUsers(pageable)).build();
  }
}
