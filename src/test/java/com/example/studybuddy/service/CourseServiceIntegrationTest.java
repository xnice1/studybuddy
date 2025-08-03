package com.example.studybuddy.service;

import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class CourseServiceIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private User instructor;
    private Course baseCourse;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        userRepository.deleteAll();

        instructor = new User();
        instructor.setUsername("instructor");
        instructor.setPassword("irrelevant");
        instructor.setRole("ADMIN");
        instructor = userRepository.save(instructor);

        baseCourse = new Course();
        baseCourse.setTitle("Math 101");
        baseCourse.setDescription("Basic arithmetic");
        baseCourse.setOwner(instructor);
    }

    @Test
    void save_persistsCourseWithOwner() {
        Course saved = courseService.save(baseCourse);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Math 101");
        assertThat(saved.getOwner().getId()).isEqualTo(instructor.getId());

        assertThat(courseRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findAll_returnsAllCourses() {
        courseService.save(baseCourse);

        Course other = new Course();
        other.setTitle("Chemistry");
        other.setDescription("Mendeleev");
        other.setOwner(instructor);
        courseService.save(other);

        List<Course> all = courseService.findAll();
        assertThat(all).hasSize(2)
                .extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Math 101", "Chemistry");
    }

    @Test
    void findById_existing_returnsCourse() {
        Course saved = courseService.save(baseCourse);

        Course fetched = courseService.findById(saved.getId());
        assertThat(fetched.getTitle()).isEqualTo("Math 101");
    }

    @Test
    void findById_missing_throws() {
        assertThatThrownBy(() -> courseService.findById(9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void update_modifiesFields() {
        Course saved = courseService.save(baseCourse);

        Course upd = new Course();
        upd.setTitle("Advanced Math");
        upd.setDescription("Geometry");

        User alt = new User();
        alt.setUsername("alt");
        alt.setPassword("x");
        alt.setRole("STUDENT");
        alt = userRepository.save(alt);
        upd.setOwner(alt);

        Course result = courseService.update(saved.getId(), upd);

        assertThat(result.getTitle()).isEqualTo("Advanced Math");
        assertThat(result.getDescription()).isEqualTo("Geometry");
        assertThat(result.getOwner().getId()).isEqualTo(alt.getId());

        Course fromDb = courseRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getOwner().getUsername()).isEqualTo("alt");
    }

    @Test
    void deleteById_removesCourse() {
        Course saved = courseService.save(baseCourse);
        assertThat(courseRepository.count()).isOne();

        courseService.deleteById(saved.getId());
        assertThat(courseRepository.count()).isZero();
    }
}
