package com.bookinghealth.api.mapper;

import com.bookinghealth.api.dto.request.admin.UserCreationRequest;
import com.bookinghealth.api.dto.response.UserResponse;
import com.bookinghealth.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
}
