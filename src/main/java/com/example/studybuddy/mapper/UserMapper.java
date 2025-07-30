package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.RegistrationRequest;
import org.mapstruct.Mapper;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.model.User;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    @Mapping(target = "role", ignore = true)
    User toEntity(RegistrationRequest request);
}
