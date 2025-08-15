package com.example.studybuddy.controller;

import com.example.studybuddy.model.User;
import com.example.studybuddy.dto.RegistrationRequest;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.QuestionRepository;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;




@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CourseRepository courseRepo;
    @Autowired private QuizRepository quizRepo;
    @Autowired private QuestionRepository questionRepo;

    @BeforeEach
    void setUp() {
        questionRepo.deleteAll();
        quizRepo.deleteAll();
        courseRepo.deleteAll();
        userRepository.deleteAll();
        User u = new User();
        u.setUsername("jane");
        u.setPassword(passwordEncoder.encode("letMeIn123"));
        u.setRole("STUDENT");
        userRepository.save(u);
    }

    @Test
    void register_newUser_returns201AndSaves() throws Exception {
        long before = userRepository.count();

        RegistrationRequest req = new RegistrationRequest();
        req.setUsername("new_user");
        req.setPassword("securePass123");
        req.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new_user"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        assertThat(userRepository.count()).isEqualTo(before + 1);
        User saved = userRepository.findByUsername("new_user").orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("new_user");
        assertThat(passwordEncoder.matches("securePass123", saved.getPassword()))
                .as("PasswordEncoder should match raw->encoded")
                .isTrue();
    }

    @Test
    void register_duplicateUser_returns409() throws Exception {
        RegistrationRequest r1 = new RegistrationRequest();
        r1.setUsername("john");
        r1.setPassword("pw12345");
        r1.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidPassword_returns400() throws Exception {
        RegistrationRequest req = new RegistrationRequest();
        req.setUsername("u2");
        req.setPassword("123");
        req.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void login_validCredentials_returns200AndToken() throws Exception {
        var creds = Map.of("username", "jane", "password", "letMeIn123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }
    @Test
    void login_badCredentials_returns401() throws Exception {
        var u = new com.example.studybuddy.model.User();
        u.setUsername("login2");
        u.setPassword(passwordEncoder.encode("rightpw"));
        u.setRole("STUDENT");
        userRepository.save(u);

        RegistrationRequest creds = new RegistrationRequest();
        creds.setUsername("login2");
        creds.setPassword("wrongpw");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void login_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"whatever\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username is required"));
    }
}
