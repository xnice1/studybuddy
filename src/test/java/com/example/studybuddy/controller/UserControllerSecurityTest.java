package com.example.studybuddy.controller;

import com.example.studybuddy.controller.UserController;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.mapper.UserMapper;
import com.example.studybuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerStandaloneTest {

    @Mock UserService userService;
    @Mock UserMapper  userMapper;

    @InjectMocks
    UserController userController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    void listUsers_returns200() throws Exception {
        var dummy = new UserResponse(1L, "alice", "STUDENT");
        when(userService.findAll()).thenReturn(List.of());
        when(userMapper.toResponse(any())).thenReturn(dummy);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }
}