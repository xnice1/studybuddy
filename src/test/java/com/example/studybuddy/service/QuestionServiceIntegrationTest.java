package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateQuestionDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.QuestionRepository;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

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
    private Course course;
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

        course = new Course();
        course.setTitle("Biology 101");
        course.setDescription("Intro to Biology");
        course.setOwner(owner);
        course = courseRepo.save(course);

        quiz = new Quiz();
        quiz.setTitle("Chapter 1 Quiz");
        quiz.setCourse(course);
        quiz = quizRepo.save(quiz);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsOwner() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(owner.getUsername())
                .password(owner.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))
                .build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticateAsAdmin() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password("x")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void findAll_emptyInitially() {
        List<Question> all = questionService.findAllByQuiz(quiz.getId());
        assertThat(all).isEmpty();
    }

    @Test
    void create_validQuestionPersists() {
        authenticateAsOwner();

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setText("What is DNA?");
        dto.setOptions(List.of("Acid", "Molecule", "Protein"));
        dto.setCorrectAnswers(List.of(1));

        Question saved = questionService.createQuestion(quiz.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(questionRepo.count()).isEqualTo(1);
        assertThat(saved.getQuiz().getId()).isEqualTo(quiz.getId());
    }

    @Test
    void create_unknownQuizThrows() {
        authenticateAsOwner();

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setText("Oops?");
        dto.setOptions(List.of("A","B"));
        dto.setCorrectAnswers(List.of(0));

        assertThatThrownBy(() -> questionService.createQuestion(9999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void create_invalidCorrectIndexThrows() {
        authenticateAsOwner();

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setText("Bad index");
        dto.setOptions(List.of("X","Y"));
        dto.setCorrectAnswers(List.of(2));

        assertThatThrownBy(() -> questionService.createQuestion(quiz.getId(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("index");
    }

    @Test
    void findById_missingThrows() {
        assertThatThrownBy(() -> questionService.findById(42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void update_changesTextOptionsAndAnswers() {
        authenticateAsOwner();

        // create initial question using API-style create (ensures ownership checks)
        CreateQuestionDTO createDto = new CreateQuestionDTO();
        createDto.setText("Old?");
        createDto.setOptions(List.of("A","B"));
        createDto.setCorrectAnswers(List.of(0));
        Question saved = questionService.createQuestion(quiz.getId(), createDto);

        // prepare update DTO
        CreateQuestionDTO updateDto = new CreateQuestionDTO();
        updateDto.setText("New?");
        updateDto.setOptions(List.of("Yes","No","Maybe"));
        updateDto.setCorrectAnswers(List.of(2));

        Question result = questionService.updateQuestion(quiz.getId(), saved.getId(), updateDto);

        assertThat(result.getText()).isEqualTo("New?");
        assertThat(result.getOptions()).containsExactly("Yes","No","Maybe");
        assertThat(result.getCorrectAnswers()).containsExactly(2);
    }

    @Test
    void deleteById_removesQuestion() {
        authenticateAsOwner();

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setText("TBD");
        dto.setOptions(List.of("X"));
        dto.setCorrectAnswers(List.of(0));
        Question saved = questionService.createQuestion(quiz.getId(), dto);

        questionService.deleteById(quiz.getId(), saved.getId());

        assertThat(questionRepo.existsById(saved.getId())).isFalse();
    }
}
