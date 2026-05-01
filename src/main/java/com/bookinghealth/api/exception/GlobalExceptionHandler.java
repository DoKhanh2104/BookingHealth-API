package com.bookinghealth.api.exception;

import com.bookinghealth.api.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ApiResponse> handleException(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build());
  }

  @ExceptionHandler(value = AppException.class)
  ResponseEntity<ApiResponse> handleAppException(AppException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return ResponseEntity.status(errorCode.getStatusCode())
        .body(
            ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
  }
}
