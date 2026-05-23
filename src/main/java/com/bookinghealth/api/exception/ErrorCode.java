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
  INVALID_KEY(10000, HttpStatus.BAD_REQUEST, "Invalid key"),
  USER_EXISTED(1001, HttpStatus.BAD_REQUEST, "Người dùng đã tồn tại"),
  PHONE_EXISTED(1017, HttpStatus.BAD_REQUEST, "Số điện thoại đã được sử dụng"),
  EMAIL_EXISTED(1018, HttpStatus.BAD_REQUEST, "Email đã được sử dụng"),
  USER_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
  UNAUTHENTICATED(1003, HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để thực hiện"),
  UNAUTHORIZED(1004, HttpStatus.FORBIDDEN, "Không có quyền truy cập"),
  INVALID_EMAIL(1005, HttpStatus.BAD_REQUEST, "Email không đúng định dạng"),
  INVALID_PASSWORD(1006, HttpStatus.BAD_REQUEST, "Mật khẩu phải có ít nhất 8 ký tự"),
  INVALID_PHONE(1007, HttpStatus.BAD_REQUEST, "Số điện thoại phải từ 10-11 số"),
  PHONE_REQUIRE(1008, HttpStatus.BAD_REQUEST, "Số điên thoại là bắt buộc"),
  UPLOAD_FILE_FAILED(1009, HttpStatus.BAD_REQUEST, "Tạo ảnh đại diện không thành công"),
  SPECIALTY_NOT_FOUND(1010, HttpStatus.NOT_FOUND, "Không tìm thấy chuyên khoa"),
  SPECIALTY_EXISTED(1011, HttpStatus.BAD_REQUEST, "Tên chuyên khoa đã tòn tại"),
  SPECIALTY_NAME_REQUIRE(1012, HttpStatus.BAD_REQUEST, "Tên chuyên khoa là bắt buộc"),
  ADDRESS_REQUIRE(1013, HttpStatus.BAD_REQUEST, "Địa chỉ là bắt buộc"),
  DOCTOR_NOT_FOUND(1014, HttpStatus.NOT_FOUND, "Không tìm thấy bác sĩ"),
  TIME_SLOT_EXISTED(1015, HttpStatus.BAD_REQUEST, "Khung giờ đã tồn tại"),
  TIME_SLOT_NOT_FOUND(1016, HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ"),
  GOOGLE_LOGIN_FAILED(1019, HttpStatus.BAD_REQUEST, "Đăng nhập Google thất bại"),
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
