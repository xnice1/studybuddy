package com.example.studybuddy.service;

import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User alice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alice = new User();
        alice.setId(1L);
        alice.setUsername("alice1");
        alice.setPassword("rawpass");
        alice.setRole("STUDENT");
    }

    @Test
    void save_encodesPasswordAndSaves() {
        when(passwordEncoder.encode("rawpass")).thenReturn("enc");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.save(alice);

        assertThat(saved.getPassword()).isEqualTo("enc");
        verify(userRepository).save(saved);
    }

    @Test
    void findById_existing_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        User u = userService.findById(1L);
        assertThat(u).isSameAs(alice);
    }

    @Test
    void findById_missing_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void update_changesFieldsAndReEncodes() {
        User updated = new User();
        updated.setUsername("alice2");
        updated.setPassword("newpass");
        updated.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(passwordEncoder.encode("newpass")).thenReturn("encNew");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(1L, updated);
        assertThat(result.getUsername()).isEqualTo("alice2");
        assertThat(result.getPassword()).isEqualTo("encNew");
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void deleteById_delegates() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteById(1L);
        verify(userRepository).deleteById(1L);
    }
}
