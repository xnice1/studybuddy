package com.example.studybuddy.service;

import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import com.example.studybuddy.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, CourseRepository courseRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        String encoded_password = passwordEncoder.encode(user.getPassword());
        user.setPassword(encoded_password);
        return userRepository.save(user);
    }

    public User update(Long id, User updated) {
        User existing = findById(id);
        existing.setUsername(updated.getUsername());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(updated.getPassword());
            existing.setPassword(encoded);

        }
        existing.setRole(updated.getRole());
        return userRepository.save(existing);
    }
    public void deleteById(Long id) {
        if (courseRepository.existsByOwnerId(id)) {
            throw new IllegalStateException("User owns one or more courses and cannot be deleted");
        }

        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw ex;
        }
    }
}
