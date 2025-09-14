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

    @Mapping(target = "courseId", source = "course")
    QuizDTO toDto(Quiz quiz);

    @Mapping(target = "courseId", source = "course")
    Quiz fromDto(QuizDTO dto);
}
