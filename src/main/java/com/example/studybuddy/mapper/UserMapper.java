package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.RegistrationRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "role", defaultValue = "STUDENT")
    User toEntity(RegistrationRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "role", defaultValue = "STUDENT")
    User fromDto(RegistrationRequest dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "role", source = "role")
    UserResponse toResponse(User user);
}
