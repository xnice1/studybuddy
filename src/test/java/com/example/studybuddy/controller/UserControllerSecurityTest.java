package com.example.studybuddy.controller;

import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.model.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User alice = new User();
        alice.setUsername("alice");
        alice.setPassword("irrelevant");
        alice.setRole("ADMIN");
        userRepository.save(alice);

        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword("irrelevant");
        bob.setRole("STUDENT");
        userRepository.save(bob);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void listUsers_returnsAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void listUsers_responseMapsToDto() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        UserResponse[] responses = objectMapper.readValue(json, UserResponse[].class);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(UserResponse::getUsername)
                .containsExactlyInAnyOrder("alice", "bob");
    }
}
