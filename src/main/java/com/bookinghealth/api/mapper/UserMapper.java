package com.bookinghealth.api.mapper;

import com.bookinghealth.api.dto.request.admin.UserCreationRequest;
import com.bookinghealth.api.dto.request.admin.UserUpdateRequest;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "avatar", ignore = true)
  User toUser(UserCreationRequest request);

  UserResponse toUserResponse(User user);

  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "avatar", ignore = true)
  void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
