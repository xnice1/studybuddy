package com.example.studybuddy.controller;

import com.example.studybuddy.dto.UserDTO;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.mapper.UserMapper;
import com.example.studybuddy.model.User;
import com.example.studybuddy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "INSTRUCTOR", "STUDENT");

    public UserController(UserService userService, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserDTO dto) {
        if (userService.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", Map.of("password", "Password is required")));
        }
        if (dto.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", Map.of("password", "Password must be at least 6 chars")));
        }

        String role = (dto.getRole() == null || dto.getRole().isBlank())
                ? "STUDENT"
                : dto.getRole().toUpperCase(Locale.ROOT);

        if (!ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", Map.of("role", "Role must be one of: " + ALLOWED_ROLES)));
        }

        User entity = new User();
        entity.setUsername(dto.getUsername());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setRole(role);

        User saved = userService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto
    ) {
        User existing = userService.findById(id);

        if (!existing.getUsername().equals(dto.getUsername())
                && userService.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        existing.setUsername(dto.getUsername());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("errors", Map.of("password", "Password must be at least 6 chars")));
            }
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            String role = dto.getRole().toUpperCase(Locale.ROOT);
            if (!ALLOWED_ROLES.contains(role)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("errors", Map.of("role", "Role must be one of: " + ALLOWED_ROLES)));
            }
            existing.setRole(role);
        }

        User updated = userService.update(id, existing);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
