package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedRole;
import com.bookinghealth.api.constant.PredefinedStatus;
import com.bookinghealth.api.dto.request.admin.UserCreationRequest;
import com.bookinghealth.api.dto.request.admin.UserUpdateRequest;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.entity.Role;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.mapper.UserMapper;
import com.bookinghealth.api.repository.RoleRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.util.HashSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {

  UserRepository userRepository;
  UserMapper userMapper;
  PasswordEncoder passwordEncoder;
  RoleRepository roleRepository;
  CloudinaryService cloudinaryService;

  // Create user
  public UserResponse createUser(UserCreationRequest request) {
    if (userRepository.existsByPhone(request.getPhone())) {
      throw new AppException(ErrorCode.USER_EXISTED);
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AppException(ErrorCode.USER_EXISTED);
    }

    User user = userMapper.toUser(request);

    user.setPassword(passwordEncoder.encode(request.getPassword()));

    // Upload avatar to Cloudinary if provided
    try {
      if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
        String avatarUrl = cloudinaryService.uploadFileAndGetUrl(request.getAvatar());
        user.setAvatar(avatarUrl);
      }
    } catch (Exception e) {
      throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
    }

    HashSet<Role> roles = new HashSet<>();
    roleRepository.findByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);
    user.setRoles(roles);

    user.setStatus(PredefinedStatus.ACTIVE);

    try {
      userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      throw new AppException(ErrorCode.USER_EXISTED);
    }

    return userMapper.toUserResponse(user);
  }

  // Update user
  public UserResponse updateUser(Long userId, UserUpdateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
      throw new AppException(ErrorCode.USER_EXISTED);
    }

    userMapper.updateUser(user, request);

    if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
      try {
        String avatarUrl = cloudinaryService.uploadFileAndGetUrl(request.getAvatar());
        user.setAvatar(avatarUrl);
      } catch (Exception e) {
        throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
      }
    }

    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      var roles = roleRepository.findAllByRoleNameIn(request.getRoles());
      user.setRoles(new HashSet<>(roles));
    } else {
      HashSet<Role> roles = new HashSet<>();
      roleRepository.findByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);
      user.setRoles(roles);
    }

    return userMapper.toUserResponse(userRepository.save(user));
  }

  // Delete user
  public void deleteUser(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    if (user.getStatus().equals(PredefinedStatus.INACTIVE)) {
      throw new AppException(ErrorCode.USER_NOT_FOUND);
    }
    user.setStatus(PredefinedStatus.INACTIVE);
    userRepository.save(user);
  }

  // Get user by id
  public UserResponse getUserById(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    return userMapper.toUserResponse(user);
  }

  // Get a list of users
  public Page<UserResponse> getUsers(Pageable pageable) {
    Page<User> userPage = userRepository.findAll(pageable);
    return userPage.map(userMapper::toUserResponse);
  }

  // Get current user profile
  @Transactional(readOnly = true)
  public UserResponse getMyProfile() {
    var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
    String username = context.getAuthentication().getName();

    User user = userRepository.findByPhone(username)
        .or(() -> userRepository.findByEmail(username))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    return userMapper.toUserResponse(user);
  }

  // Update current user profile
  public UserResponse updateMyProfile(com.bookinghealth.api.dto.request.client.UpdateProfileRequest request) {
    var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
    String username = context.getAuthentication().getName();

    User user = userRepository.findByPhone(username)
        .or(() -> userRepository.findByEmail(username))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
      String newPhone = request.getPhone().trim();
      if (user.getPhone() != null && !user.getPhone().isEmpty()) {
        if (!user.getPhone().equals(newPhone)) {
          throw new AppException(ErrorCode.UNAUTHORIZED);
        }
      } else {
        if (userRepository.existsByPhone(newPhone)) {
          throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        user.setPhone(newPhone);
      }
    }

    if (request.getName() != null && !request.getName().trim().isEmpty()) {
      user.setName(request.getName().trim());
    }

    return userMapper.toUserResponse(userRepository.save(user));
  }

  // Upload current user avatar
  public String uploadMyAvatar(org.springframework.web.multipart.MultipartFile file) {
    var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
    String username = context.getAuthentication().getName();

    User user = userRepository.findByPhone(username)
        .or(() -> userRepository.findByEmail(username))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    try {
      String avatarUrl = cloudinaryService.uploadFileAndGetUrl(file);
      user.setAvatar(avatarUrl);
      userRepository.save(user);
      return avatarUrl;
    } catch (Exception e) {
      throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
    }
  }
}
