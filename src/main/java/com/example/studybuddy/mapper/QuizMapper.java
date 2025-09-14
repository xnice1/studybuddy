package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    // DTO → Entity
    @Mapping(target = "course", source = "courseId", qualifiedByName = "idToCourse")
    Quiz toEntity(QuizDTO dto);

    // Entity → DTO
    @Mapping(target = "courseId", source = "course.id")
    QuizDTO toDto(Quiz quiz);

    // Alias (if you need fromDto name)
    default Quiz fromDto(QuizDTO dto) {
        return toEntity(dto);
    }

    // Helper for courseId → Course
    @Named("idToCourse")
    default Course idToCourse(Long id) {
        if (id == null) return null;
        Course c = new Course();
        c.setId(id);
        return c;
    }
}
