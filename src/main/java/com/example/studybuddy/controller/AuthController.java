package com.example.studybuddy.controller;

import com.example.studybuddy.dto.RegistrationRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.mapper.UserMapper;
import com.example.studybuddy.model.User;
import com.example.studybuddy.security.JwtUtils;
import com.example.studybuddy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authManager,
                          JwtUtils jwtUtils,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          UserMapper userMapper) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegistrationRequest req
    ) {
        if (userService.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User entity = userMapper.toEntity(req);
        User saved = userService.save(entity);
        UserResponse resp = userMapper.toResponse(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody RegistrationRequest creds) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(), creds.getPassword()
                    )
            );
            String token = jwtUtils.generateToken(auth.getName());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException ex) {
            logger.warn("Bad credentials for user {}", creds.getUsername(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception ex) {
            logger.error("Unexpected error during login", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getClass().getSimpleName(),
                            "message", ex.getMessage()));
        }
    }
}
