package com.bookinghealth.api.controller;

import com.bookinghealth.api.dto.request.AuthenticationRequest;
import com.bookinghealth.api.dto.request.IntrospectRequest;
import com.bookinghealth.api.dto.request.client.DoctorSignupRequest;
import com.bookinghealth.api.dto.request.client.ForgotPasswordRequest;
import com.bookinghealth.api.dto.request.client.GoogleLoginRequest;
import com.bookinghealth.api.dto.request.client.ResetPasswordRequest;
import com.bookinghealth.api.dto.request.client.SignupRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.AuthenticationResponse;
import com.bookinghealth.api.dto.response.IntrospectResponse;
import com.bookinghealth.api.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import java.text.ParseException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

  AuthenticationService authenticationService;

  @PostMapping("/login")
  public ApiResponse<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authenticationService.authenticate(request))
        .build();
  }

  @PostMapping("/introspect")
  public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
      throws ParseException, JOSEException {
    return ApiResponse.<IntrospectResponse>builder()
        .result(authenticationService.introspect(request))
        .build();
  }

  @PostMapping("/signup")
  public ApiResponse<AuthenticationResponse> register(@RequestBody SignupRequest request) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authenticationService.register(request))
        .build();
  }

  @PostMapping(
      value = "/signup-doctor",
      consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<AuthenticationResponse> registerDoctor(
      @Valid @ModelAttribute DoctorSignupRequest request) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authenticationService.registerDoctor(request))
        .build();
  }

  @PostMapping("/google")
  public ApiResponse<AuthenticationResponse> loginWithGoogle(
      @RequestBody GoogleLoginRequest request) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authenticationService.loginWithGoogle(request))
        .build();
  }

  @PostMapping("/forgot-password")
  public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

    authenticationService.forgotPassword(request);
    return ApiResponse.<Void>builder().build();
  }

  @PostMapping("/reset-password")
  public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authenticationService.resetPassword(request);
    return ApiResponse.<Void>builder().build();
  }
}
