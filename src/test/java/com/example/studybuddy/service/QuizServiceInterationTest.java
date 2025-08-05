package com.example.studybuddy.service;

import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@Import(QuizService.class)
class QuizServiceIntegrationTest {

    @Autowired private QuizService quizService;
    @Autowired private CourseRepository courseRepo;
    @Autowired private QuizRepository quizRepo;
    @Autowired private UserRepository userRepo;
    private Course savedCourse;
    private User savedOwner;

    @BeforeEach
    void setUp() {
        quizRepo.deleteAll();
        courseRepo.deleteAll();
        userRepo.deleteAll();

        User owner = new User();
        owner.setUsername("courseOwner");
        owner.setPassword("irrelevant");
        owner.setRole("INSTRUCTOR");
        savedOwner = userRepo.save(owner);

        Course c = new Course();
        c.setTitle("Geography");
        c.setDescription("World facts");
        c.setOwner(savedOwner);
        savedCourse = courseRepo.save(c);
    }

    @Test
    void findAll_empty_atStart() {
        List<Quiz> all = quizService.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void save_validQuiz_isPersisted() {
        Quiz q = new Quiz();
        q.setTitle("Capitals Quiz");
        q.setCourse(savedCourse);

        Quiz saved = quizService.save(q);

        assertThat(saved.getId()).isNotNull();
        assertThat(quizRepo.findById(saved.getId())).isPresent();
        assertThat(saved.getTitle()).isEqualTo("Capitals Quiz");
        assertThat(saved.getCourse().getId()).isEqualTo(savedCourse.getId());
    }

    @Test
    void save_missingCourse_throws() {
        Quiz q = new Quiz();
        q.setTitle("No Course Quiz");
        Course fake = new Course();
        fake.setId(999L);
        q.setCourse(fake);

        assertThatThrownBy(() -> quizService.save(q))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Course not found with id 999");
    }

    @Test
    void findById_existing_returnsQuiz() {
        Quiz q = new Quiz();
        q.setTitle("Geo Quiz");
        q.setCourse(savedCourse);
        Quiz persisted = quizRepo.save(q);

        Quiz fromService = quizService.findById(persisted.getId());
        assertThat(fromService.getId()).isEqualTo(persisted.getId());
    }

    @Test
    void findById_missing_throws() {
        assertThatThrownBy(() -> quizService.findById(42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Quiz not found with id 42");
    }

    @Test
    void update_changesTitleAndKeepsCourse() {
        Quiz q = new Quiz();
        q.setTitle("Old title");
        q.setCourse(savedCourse);
        Quiz saved = quizRepo.save(q);

        Quiz update = new Quiz();
        update.setTitle("New title");
        update.setCourse(null);

        Quiz result = quizService.update(saved.getId(), update);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getCourse().getId()).isEqualTo(savedCourse.getId());
    }

    @Test
    void update_changeCourseToAnother() {
        Quiz q = new Quiz();
        q.setTitle("Switch Course");
        q.setCourse(savedCourse);
        Quiz savedQuiz = quizRepo.save(q);

        Course c2 = new Course();
        c2.setTitle("Biology");
        c2.setDescription("Life sciences");
        c2.setOwner(savedOwner);
        Course saved2 = courseRepo.save(c2);

        Quiz update = new Quiz();
        update.setTitle("Switch Course");
        Course proxy = new Course();
        proxy.setId(saved2.getId());
        update.setCourse(proxy);

        Quiz result = quizService.update(savedQuiz.getId(), update);

        assertThat(result.getCourse().getId()).isEqualTo(saved2.getId());
    }


    @Test
    void update_unknownCourse_throws() {
        Quiz q = new Quiz();
        q.setTitle("Bad update");
        q.setCourse(savedCourse);
        Quiz saved = quizRepo.save(q);

        Quiz update = new Quiz();
        update.setTitle("Bad update");
        Course bogus = new Course();
        bogus.setId(555L);
        update.setCourse(bogus);

        assertThatThrownBy(() -> quizService.update(saved.getId(), update))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Course not found with id 555");
    }

    @Test
    void deleteById_removes() {
        Quiz q = new Quiz();
        q.setTitle("To be deleted");
        q.setCourse(savedCourse);
        Quiz saved = quizRepo.save(q);

        quizService.deleteById(saved.getId());

        assertThat(quizRepo.existsById(saved.getId())).isFalse();
    }
}
