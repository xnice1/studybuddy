package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.model.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    QuizMapper INSTANCE = Mappers.getMapper(QuizMapper.class);
    @Mapping(target = "courseId", source = "courseId")
    Quiz toEntity(QuizDTO dto);

    @Mapping(target = "courseId", source = "courseId")
    QuizDTO toDto(Quiz quiz);

    @Mapping(target = "courseId", source = "courseId")
    Quiz fromDto(QuizDTO dto);
}
