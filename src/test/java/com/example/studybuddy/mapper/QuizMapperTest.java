package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Quiz;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class QuizMapperTest {

    private final QuizMapper mapper = Mappers.getMapper(QuizMapper.class);

    @Test
    void quiz_to_dto_mapsCourseId() {
        Course c = new Course();
        c.setId(42L);
        Quiz q = new Quiz();
        q.setId(2L);
        q.setTitle("Midterm");
        q.setCourse(c);

        QuizDTO dto = mapper.toDto(q);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getTitle()).isEqualTo("Midterm");
        assertThat(dto.getCourseId()).isEqualTo(42L);
    }

    @Test
    void dto_to_quiz_createsCourseStub() {
        QuizDTO dto = new QuizDTO();
        dto.setTitle("Weekly");
        dto.setCourseId(101L);

        Quiz q = mapper.fromDto(dto);

        assertThat(q).isNotNull();
        assertThat(q.getTitle()).isEqualTo("Weekly");
        assertThat(q.getCourse()).isNotNull();
        assertThat(q.getCourse().getId()).isEqualTo(101L);
    }
}
