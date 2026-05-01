package com.bookinghealth.api.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, HttpStatus.INTERNAL_SERVER_ERROR, "Uncategorized exception"),
  USER_EXISTED(1001, HttpStatus.BAD_REQUEST, "User already exists"),
  USER_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "User not found"),
  UNAUTHENTICATED(1003, HttpStatus.UNAUTHORIZED, "Unauthenticated"),
  UNAUTHORIZED(1004, HttpStatus.FORBIDDEN, "You do not have permission to perform this operation"),
  ;

  ErrorCode(int code, HttpStatusCode statusCode, String message) {
    this.code = code;
    this.statusCode = statusCode;
    this.message = message;
  }

  int code;
  HttpStatusCode statusCode;
  String message;
}
