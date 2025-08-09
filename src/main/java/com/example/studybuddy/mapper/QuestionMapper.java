package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.QuestionDTO;
import com.example.studybuddy.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
public interface QuestionMapper {

    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz.id", source = "quizId")
    Question fromDto(QuestionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz.id", source = "quizId")
    default Question toEntity(QuestionDTO dto) {
        return fromDto(dto);
    }

    @Mapping(target = "quizId", source = "quiz.id")
    QuestionDTO toDto(Question question);
}
