package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.model.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    QuizMapper INSTANCE = Mappers.getMapper(QuizMapper.class);

    Quiz toEntity(QuizDTO dto);

    @Mapping(target = "course_id", source = "courseId")
    QuizDTO toDto(Quiz quiz);

    @Mapping(target = "course_id", source = "courseId")
    Quiz fromDto(QuizDTO dto);
}
