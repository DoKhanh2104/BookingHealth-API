package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedRole;
import com.bookinghealth.api.constant.PredefinedStatus;
import com.bookinghealth.api.dto.request.admin.UserCreationRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.entity.Role;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.mapper.UserMapper;
import com.bookinghealth.api.repository.RoleRepository;
import com.bookinghealth.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Service
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    CloudinaryService cloudinaryService;

    public UserResponse createUser(UserCreationRequest request) {
        if(userRepository.existsByPhone(request.getPhone())) {
            throw  new AppException(ErrorCode.USER_EXISTED);
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw  new AppException(ErrorCode.USER_EXISTED);
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

        user.setStatus(PredefinedStatus.ACTIVE);

        try{
            userRepository.save(user);
        }catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }
}
