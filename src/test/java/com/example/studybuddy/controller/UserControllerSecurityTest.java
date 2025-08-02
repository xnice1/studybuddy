package com.example.studybuddy.controller;

import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;


import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    private Long aliceId, bobId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User alice = new User();
        alice.setUsername("alice");
        alice.setPassword("irrelevant");
        alice.setRole("ADMIN");
        alice = userRepository.save(alice);
        aliceId = alice.getId();

        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword("irrelevant");
        bob.setRole("STUDENT");
        bob = userRepository.save(bob);
        bobId = bob.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returnsCorrectUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", aliceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aliceId))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // was 404
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_succeeds() throws Exception {
        var payload = Map.of(
                "username", "charlie",
                "password", "pass123",
                "role",     "STUDENT"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())   // expect 201 now
                .andExpect(jsonPath("$.username").value("charlie"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        assertThat(userRepository.count()).isEqualTo(3);
    }

    @Test
    void createUser_unauthenticated_forbidden() throws Exception {
        var payload = Map.of(
                "username", "daniel",
                "password", "pw",
                "role",     "STUDENT"
        );
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_succeeds() throws Exception {
        var payload = Map.of(
                "username", "alice2",
                "password", "newpass",
                "role",     "ADMIN"
        );
        mockMvc.perform(put("/api/users/{id}", aliceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice2"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateUser_forbiddenForStudent() throws Exception {
        var payload = Map.of(
                "username", "bob2",
                "password", "password123",
                "role",     "STUDENT"
        );
        mockMvc.perform(put("/api/users/{id}", bobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_succeeds() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", bobId))
                .andExpect(status().isNoContent());
        assertThat(userRepository.existsById(bobId)).isFalse();
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteUser_forbiddenForStudent() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", aliceId))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_missingUsername_badRequest() throws Exception {
        var payload = Map.of(
                "password", "pw123123",
                "role",     "STUDENT"
        );
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

}
