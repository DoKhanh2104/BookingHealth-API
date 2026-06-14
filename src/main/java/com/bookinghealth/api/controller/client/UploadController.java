package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.service.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/upload")
public class UploadController {

  CloudinaryService cloudinaryService;

  @PostMapping
  public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
    String fileUrl = cloudinaryService.uploadFileAndGetUrl(file);
    return ApiResponse.<String>builder().result(fileUrl).build();
  }
}
