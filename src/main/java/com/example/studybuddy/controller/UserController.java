package com.example.studybuddy.controller;

import com.example.studybuddy.dto.RegistrationRequest;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class  UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
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
    public ResponseEntity<UserResponse> create(@Valid @RequestBody RegistrationRequest req) {
        User entity = userMapper.toEntity(req);
        entity.setPassword(passwordEncoder.encode(req.getPassword()));
        User saved = userService.save(entity);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userMapper.toResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RegistrationRequest req
    ) {
        User toUpdate = new User();
        toUpdate.setUsername(req.getUsername());
        toUpdate.setPassword(passwordEncoder.encode(req.getPassword()));
        toUpdate.setRole(req.getRole());
        User updated = userService.update(id, toUpdate);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
