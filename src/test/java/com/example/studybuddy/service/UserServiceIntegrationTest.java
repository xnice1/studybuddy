package com.example.studybuddy.service;

import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User alice;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        alice = new User();
        alice.setUsername("alice");
        alice.setPassword("rawpass");
        alice.setRole("STUDENT");
    }

    @Test
    void save_encodesPasswordAndSaves() {
        User saved = userService.save(alice);


        AbstractStringAssert<?> rawpass = assertThat(saved.getPassword()).isNotEqualTo("rawpass");
        assertThat(passwordEncoder.matches("rawpass", saved.getPassword())).isTrue();
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void findById_existing_returnsUser() {
        alice.setPassword(passwordEncoder.encode("x"));
        alice = userRepository.save(alice);

        User found = userService.findById(alice.getId());
        assertThat(found).isNotNull()
                .extracting(User::getUsername)
                .isEqualTo("alice");
    }

    @Test
    void findById_missing_throws() {
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void update_changesFieldsAndReEncodes() {
        alice.setPassword(passwordEncoder.encode("initial"));
        alice = userRepository.save(alice);

        User updated = new User();
        updated.setUsername("alice2");
        updated.setPassword("newpass");
        updated.setRole("ADMIN");

        User result = userService.update(alice.getId(), updated);

        assertThat(result.getUsername()).isEqualTo("alice2");
        assertThat(passwordEncoder.matches("newpass", result.getPassword())).isTrue();
        assertThat(result.getRole()).isEqualTo("ADMIN");

        User fromDb = userRepository.findById(alice.getId()).orElseThrow();
        assertThat(fromDb.getUsername()).isEqualTo("alice2");
    }

    @Test
    void deleteById_delegates() {
        alice.setPassword(passwordEncoder.encode("pw"));
        alice = userRepository.save(alice);
        assertThat(userRepository.count()).isEqualTo(1);

        userService.deleteById(alice.getId());
        assertThat(userRepository.count()).isZero();
    }
}
