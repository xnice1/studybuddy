package com.example.studybuddy.security;

import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.QuestionRepository;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private QuestionRepository questionRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtils jwtUtils;

    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        if (questionRepo != null) questionRepo.deleteAll();
        if (quizRepo != null) quizRepo.deleteAll();
        if (courseRepo != null) courseRepo.deleteAll();
        userRepo.deleteAll();

        User student = new User();
        student.setUsername("stu");
        student.setPassword("doesn't matter");
        student.setRole("STUDENT");
        userRepo.save(student);

        User admin = new User();
        admin.setUsername("boss");
        admin.setPassword("also doesn't matter");
        admin.setRole("ADMIN");
        userRepo.save(admin);

        studentToken = "Bearer " + jwtUtils.generateToken(student.getUsername());
        adminToken   = "Bearer " + jwtUtils.generateToken(admin.getUsername());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void protectedEndpoint_withStudentToken_returns403() throws Exception {
        String validCourseJson = """
    {
      "title":       "Some Course",
      "description": "Some Course description",
      "ownerId":     1
    }
    """;
        mockMvc.perform(post("/api/courses")
                        .header(HttpHeaders.AUTHORIZATION, studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCourseJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withAdminToken_returns200() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk());
    }
}
