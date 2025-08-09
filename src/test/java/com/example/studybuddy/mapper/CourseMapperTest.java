package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CourseMapperTest {

    private final CourseMapper mapper = Mappers.getMapper(CourseMapper.class);

    @Test
    void course_to_dto_mapsOwnerId() {
        User owner = new User();
        owner.setId(7L);
        owner.setUsername("inst");

        Course course = new Course();
        course.setId(3L);
        course.setTitle("Chem");
        course.setDescription("desc");
        course.setOwner(owner);

        CourseDTO dto = mapper.toDto(course);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getTitle()).isEqualTo("Chem");
        assertThat(dto.getOwnerId()).isEqualTo(7L);
    }

    @Test
    void dto_to_course_createsOwnerWithId() {
        CourseDTO dto = new CourseDTO();
        dto.setId(5L);
        dto.setTitle("Biology");
        dto.setOwnerId(99L);

        Course course = mapper.fromDto(dto);

        assertThat(course).isNotNull();
        assertThat(course.getTitle()).isEqualTo("Biology");
        assertThat(course.getOwner()).isNotNull();
        assertThat(course.getOwner().getId()).isEqualTo(99L);
    }
}
