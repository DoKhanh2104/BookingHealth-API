package com.bookinghealth.api.controller;

import com.bookinghealth.api.dto.request.AuthenticationRequest;
import com.bookinghealth.api.dto.request.IntrospectRequest;
import com.bookinghealth.api.dto.request.client.GoogleLoginRequest;
import com.bookinghealth.api.dto.request.client.SignupRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.AuthenticationResponse;
import com.bookinghealth.api.dto.response.IntrospectResponse;
import com.bookinghealth.api.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

  @PostMapping("/google")
  public ApiResponse<AuthenticationResponse> loginWithGoogle(
      @RequestBody GoogleLoginRequest request) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authenticationService.loginWithGoogle(request))
        .build();
  }
}
