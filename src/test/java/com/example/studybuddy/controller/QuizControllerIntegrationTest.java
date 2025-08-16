package com.example.studybuddy.controller;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class QuizControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CourseRepository courseRepo;
    @Autowired private QuizRepository quizRepo;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepo;

    Course course = new Course();

    @BeforeEach
    void setUp() {
        quizRepo.deleteAll();
        courseRepo.deleteAll();
        userRepo.deleteAll();


        User instructor = new User();
        instructor = new User();
        instructor.setUsername("inst1");
        instructor.setPassword("irrelevant");
        instructor.setRole("INSTRUCTOR");
        instructor = userRepo.save(instructor);

        
        course = new Course();
        course.setTitle("History 101");
        course.setDescription("Ancient civilizations");
        course.setOwner(instructor);
        course = courseRepo.save(course);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAll_quizzes_emptyShowsZero() throws Exception {
        mockMvc.perform(get("/api/quizzes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAll_quizzes_afterSeedingShowsThem() throws Exception {
        Quiz a = new Quiz();
        a.setTitle("Quiz A");
        a.setCourse(course);
        quizRepo.save(a);
        Quiz b = new Quiz();
        b.setTitle("Quiz B");
        b.setCourse(course);
        quizRepo.save(b);

        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.title=='Quiz A')]").exists())
                .andExpect(jsonPath("$[?(@.title=='Quiz B')]").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_existing_returnsDto() throws Exception {
        Quiz q = new Quiz();
        q.setTitle("Midterm");
        q.setCourse(course);
        Quiz saved = quizRepo.save(q);

        mockMvc.perform(get("/api/quizzes/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Midterm"))
                .andExpect(jsonPath("$.courseId").value(course.getId()));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_missing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/quizzes/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void anyUnauthenticatedEndpoint_givesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "STUDENT")
    void create_forbiddenForNonAdmin() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setTitle("New Quiz");
        dto.setCourseId(course.getId());

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_missingTitle_badRequest() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setCourseId(course.getId());

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_unknownCourse_returnsNotFound() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setTitle("Ghost Quiz");
        dto.setCourseId(9999L);

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_valid_thenCountIncreases() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setTitle("Weekly Quiz");
        dto.setCourseId(course.getId());

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Weekly Quiz"));

        assertThat(quizRepo.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_changesTitle() throws Exception {
        Quiz q = new Quiz();
        q.setTitle("Old");
        q.setCourse(course);
        Quiz saved = quizRepo.save(q);

        QuizDTO dto = new QuizDTO();
        dto.setTitle("Updated");
        dto.setCourseId(course.getId());

        mockMvc.perform(put("/api/quizzes/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void update_forbiddenForStudent() throws Exception {
        Quiz q = new Quiz();
        q.setTitle("X");
        q.setCourse(course);
        Quiz saved = quizRepo.save(q);

        QuizDTO dto = new QuizDTO();
        dto.setTitle("Y");
        dto.setCourseId(course.getId());

        mockMvc.perform(put("/api/quizzes/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_removesQuiz() throws Exception {
        Quiz q = new Quiz();
        q.setTitle("ToDelete");
        q.setCourse(course);
        Quiz saved = quizRepo.save(q);

        mockMvc.perform(delete("/api/quizzes/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(quizRepo.existsById(saved.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void delete_forbiddenForStudent() throws Exception {
        Quiz q = new Quiz();
        q.setTitle("Z");
        q.setCourse(course);
        Quiz saved = quizRepo.save(q);

        mockMvc.perform(delete("/api/quizzes/{id}", saved.getId()))
                .andExpect(status().isForbidden());
    }
}
