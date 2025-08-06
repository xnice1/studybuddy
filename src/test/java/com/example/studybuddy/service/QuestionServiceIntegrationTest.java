package com.example.studybuddy.service;

import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.QuestionRepository;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class QuestionServiceIntegrationTest {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private QuestionRepository questionRepo;

    @Autowired
    private QuestionService questionService;

    private User owner;
    private Course c;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        questionRepo.deleteAll();
        quizRepo.deleteAll();
        courseRepo.deleteAll();
        userRepo.deleteAll();

        owner = new User();
        owner.setUsername("owner1");
        owner.setPassword("irrelevant");
        owner.setRole("INSTRUCTOR");
        owner = userRepo.save(owner);

        c = new Course();
        c.setTitle("Biology 101");
        c.setDescription("Intro to Biology");
        c.setOwner(owner);
        c = courseRepo.save(c);

        quiz = new Quiz();
        quiz.setTitle("Chapter 1 Quiz");
        quiz.setCourse(c);
        quiz = quizRepo.save(quiz);
    }

    @Test
    void findAll_emptyInitially() {
        List<Question> all = questionService.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void save_validQuestionPersists() {
        Question q = new Question();
        q.setText("What is DNA?");
        q.setOptions(List.of("Acid", "Molecule", "Protein"));
        q.setCorrectAnswers(List.of(1));
        q.setQuiz(quiz);

        Question saved = questionService.save(q);

        assertThat(saved.getId()).isNotNull();
        assertThat(questionRepo.count()).isEqualTo(1);
    }

    @Test
    void save_unknownQuizThrows() {
        Question q = new Question();
        q.setText("Oops?");
        q.setOptions(List.of("A","B"));
        q.setCorrectAnswers(List.of(0));
        Quiz stub = new Quiz();
        stub.setId(9999L);
        q.setQuiz(stub);

        assertThatThrownBy(() -> questionService.save(q))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void save_invalidCorrectIndexThrows() {
        Question q = new Question();
        q.setText("Bad index");
        q.setOptions(List.of("X","Y"));
        q.setCorrectAnswers(List.of(2));
        q.setQuiz(quiz);

        assertThatThrownBy(() -> questionService.save(q))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid correct answer index");
    }

    @Test
    void findById_missingThrows() {
        assertThatThrownBy(() -> questionService.findById(42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void update_changesTextOptionsAndAnswers() {
        Question q = new Question();
        q.setText("Old?");
        q.setOptions(List.of("A","B"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        Question saved = questionRepo.save(q);

        Question upd = new Question();
        upd.setText("New?");
        upd.setOptions(List.of("Yes","No","Maybe"));
        upd.setCorrectAnswers(List.of(2));
        Question result = questionService.update(saved.getId(), upd);

        assertThat(result.getText()).isEqualTo("New?");
        assertThat(result.getOptions()).containsExactly("Yes","No","Maybe");
        assertThat(result.getCorrectAnswers()).containsExactly(2);
    }

    @Test
    void update_changeQuizToAnother() {
        Course c2 = new Course();
        c2.setTitle("Chemistry");
        c2.setDescription("Basics");
        c2.setOwner(userRepo.findAll().getFirst());
        c2 = courseRepo.save(c2);

        Quiz q2 = new Quiz();
        q2.setTitle("Chem Quiz");
        q2.setCourse(c2);
        q2 = quizRepo.save(q2);

        Question q = new Question();
        q.setText("Switch?");
        q.setOptions(List.of("1","2"));
        q.setCorrectAnswers(List.of(1));
        q.setQuiz(quiz);
        Question saved = questionRepo.save(q);

        Question upd = new Question();
        Quiz stub = new Quiz();
        stub.setId(q2.getId());
        upd.setQuiz(stub);

        Question result = questionService.update(saved.getId(), upd);

        assertThat(result.getQuiz().getId()).isEqualTo(q2.getId());
    }

    @Test
    void deleteById_removesQuestion() {
        Question q = new Question();
        q.setText("TBD");
        q.setOptions(List.of("X"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        Question saved = questionRepo.save(q);

        questionService.deleteById(saved.getId());

        assertThat(questionRepo.existsById(saved.getId())).isFalse();
    }
}
