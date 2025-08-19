package com.example.studybuddy.controller;

import com.example.studybuddy.dto.LoginRequest;
import com.example.studybuddy.dto.RegistrationRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.mapper.UserMapper;
import com.example.studybuddy.model.User;
import com.example.studybuddy.security.JwtUtils;
import com.example.studybuddy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authManager,
                          JwtUtils jwtUtils,
                          UserService userService,
                          UserMapper userMapper) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegistrationRequest req) {
        if (userService.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User entity = userMapper.toEntity(req);
        User saved = userService.save(entity);
        UserResponse resp = userMapper.toResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginJson(@Valid @RequestBody LoginRequest creds) {
        return doAuthenticate(creds.getUsername(), creds.getPassword());
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> loginForm(@RequestParam MultiValueMap<String, String> form) {
        String username = form.getFirst("username");
        String password = form.getFirst("password");
        if (username == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "username and password are required"));
        }
        return doAuthenticate(username, password);
    }


    private ResponseEntity<?> doAuthenticate(String username, String password) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String token = jwtUtils.generateToken(auth.getName());
            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException ex) {
            logger.warn("Bad credentials for user {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (AuthenticationException ex) {
            logger.warn("Authentication failed for user {}: {}", username, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "authentication_failed", "error_description", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error during login", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
        }
    }
}
