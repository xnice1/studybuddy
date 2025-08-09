package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseMapper INSTANCE = Mappers.getMapper(CourseMapper.class);

    Course toEntity(CourseDTO dto);

    @Mapping(target = "ownerId", source = "owner.id")
    CourseDTO toDto(Course course);

    @Mapping(target = "owner.id", source = "ownerId")
    @Mapping(target = "id", source = "id")
    Course fromDto(CourseDTO dto);
}
