package com.example.studybuddy.controller;

import com.example.studybuddy.dto.QuestionDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.QuestionRepository;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class QuestionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CourseRepository courseRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private QuizRepository quizRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private ObjectMapper objectMapper;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quizRepo.deleteAll();
        courseRepo.deleteAll();
        userRepo.deleteAll();

        User owner;
        owner = new User();
        owner.setUsername("owner1");
        owner.setPassword("password1");
        owner.setRole("ADMIN");
        owner = userRepo.save(owner);

        Course course = new Course();
        course.setTitle("Test Course");
        course.setDescription("Desc");
        course.setOwner(owner);
        course = courseRepo.save(course);

        quiz = new Quiz();
        quiz.setTitle("Test Quiz");
        quiz.setCourse(course);
        quiz = quizRepo.save(quiz);
    }

    @Test
    void unauthenticated_getAll_forbidden() throws Exception {
        mockMvc.perform(get("/api/quizzes/{quizId}/questions", quiz.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_empty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/quizzes/{quizId}/questions", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_afterSeeding_returnsQuestions() throws Exception {
        Question q1 = new Question();
        q1.setText("Q1");
        q1.setOptions(List.of("A","B"));
        q1.setCorrectAnswers(List.of(1));
        q1.setQuiz(quiz);
        questionRepo.save(q1);

        Question q2 = new Question();
        q2.setText("Q2");
        q2.setOptions(List.of("X","Y","Z"));
        q2.setCorrectAnswers(List.of(0,2));
        q2.setQuiz(quiz);
        questionRepo.save(q2);

        mockMvc.perform(get("/api/quizzes/{quizId}/questions", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.text=='Q1')]").exists())
                .andExpect(jsonPath("$[?(@.text=='Q2')]").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_existing_returnsDto() throws Exception {
        Question q = new Question();
        q.setText("FindMe");
        q.setOptions(List.of("1","2"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        q = questionRepo.save(q);

        mockMvc.perform(get("/api/quizzes/{quizId}/questions/{id}", quiz.getId(), q.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(q.getId()))
                .andExpect(jsonPath("$.text").value("FindMe"))
                .andExpect(jsonPath("$.quizId").value(quiz.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_missing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/quizzes/{quizId}/questions/{id}", quiz.getId(), 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_valid_createsAndReturns() throws Exception {
        QuestionDTO dto = new QuestionDTO();
        dto.setText("New?");
        dto.setOptions(List.of("Yes","No"));
        dto.setCorrectAnswers(List.of(1));
        dto.setQuizId(quiz.getId());

        mockMvc.perform(post("/api/quizzes/{quizId}/questions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New?"))
                .andExpect(jsonPath("$.options.length()").value(2))
                .andExpect(jsonPath("$.correctAnswers[0]").value(1));

        assertThat(questionRepo.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_missingText_badRequest() throws Exception {
        QuestionDTO dto = new QuestionDTO();
        dto.setOptions(List.of("A","B"));
        dto.setCorrectAnswers(List.of(0));
        dto.setQuizId(quiz.getId());

        mockMvc.perform(post("/api/quizzes/{quizId}/questions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_invalidAnswerIndex_badRequest() throws Exception {
        QuestionDTO dto = new QuestionDTO();
        dto.setText("Bad!");
        dto.setOptions(List.of("A"));
        dto.setCorrectAnswers(List.of(5));
        dto.setQuizId(quiz.getId());

        mockMvc.perform(post("/api/quizzes/{quizId}/questions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_unknownQuiz_notFound() throws Exception {
        QuestionDTO dto = new QuestionDTO();
        dto.setText("Orphan");
        dto.setOptions(List.of("A","B"));
        dto.setCorrectAnswers(List.of(0));
        dto.setQuizId(9999L);

        mockMvc.perform(post("/api/quizzes/{quizId}/questions", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void create_forbiddenForStudent() throws Exception {
        QuestionDTO dto = new QuestionDTO();
        dto.setText("X");
        dto.setOptions(List.of("A"));
        dto.setCorrectAnswers(List.of(0));
        dto.setQuizId(quiz.getId());

        mockMvc.perform(post("/api/quizzes/{quizId}/questions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_changesTextAndOptionsAndAnswers() throws Exception {
        Question q = new Question();
        q.setText("Old");
        q.setOptions(List.of("A","B"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        q = questionRepo.save(q);

        QuestionDTO dto = new QuestionDTO();
        dto.setText("New");
        dto.setOptions(List.of("Yes","No","Maybe"));
        dto.setCorrectAnswers(List.of(2));
        dto.setQuizId(quiz.getId());

        mockMvc.perform(put("/api/quizzes/{quizId}/questions/{id}", quiz.getId(), q.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("New"))
                .andExpect(jsonPath("$.options.length()").value(3))
                .andExpect(jsonPath("$.correctAnswers[0]").value(2));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void update_forbiddenForStudent() throws Exception {
        Question q = new Question();
        q.setText("Old");
        q.setOptions(List.of("A","B"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        q = questionRepo.save(q);

        QuestionDTO dto = new QuestionDTO();
        dto.setText("New");
        dto.setOptions(List.of("Yes","No"));
        dto.setCorrectAnswers(List.of(1));
        dto.setQuizId(quiz.getId());

        mockMvc.perform(put("/api/quizzes/{quizId}/questions/{id}", quiz.getId(), q.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_removesQuestion() throws Exception {
        Question q = new Question();
        q.setText("ToDelete");
        q.setOptions(List.of("A"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        q = questionRepo.save(q);

        mockMvc.perform(delete("/api/quizzes/{quizId}/questions/{id}", quiz.getId(), q.getId()))
                .andExpect(status().isNoContent());

        assertThat(questionRepo.existsById(q.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void delete_forbiddenForStudent() throws Exception {
        Question q = new Question();
        q.setText("X");
        q.setOptions(List.of("A"));
        q.setCorrectAnswers(List.of(0));
        q.setQuiz(quiz);
        q = questionRepo.save(q);

        mockMvc.perform(delete("/api/quizzes/{quizId}/questions/{id}", quiz.getId(), q.getId()))
                .andExpect(status().isForbidden());
    }
}
