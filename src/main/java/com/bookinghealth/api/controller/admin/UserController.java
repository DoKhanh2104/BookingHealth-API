package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.UserCreationRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/users")
public class UserController {
    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser (@Valid @RequestBody @ModelAttribute UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }
}
