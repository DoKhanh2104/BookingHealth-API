package com.bookinghealth.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {

  Cloudinary cloudinary;

  @NonFinal
  @Value("${cloudinary.folder-name}")
  String folderName;

  public Map uploadFile(MultipartFile file) throws IOException {
    return cloudinary
        .uploader()
        .upload(file.getBytes(), ObjectUtils.asMap("folder", folderName, "resource_type", "auto"));
  }

  public String uploadFileAndGetUrl(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }
    Map uploadResult = uploadFile(file);
    return (String) uploadResult.get("secure_url");
  }
}
