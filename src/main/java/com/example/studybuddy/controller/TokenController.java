package com.example.studybuddy.controller;

import com.example.studybuddy.security.JwtUtils;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public TokenController(AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(@RequestParam MultiValueMap<String,String> form) {
        String grantType = form.getFirst("grant_type");
        if (grantType == null || !grantType.equals("password")) {
            return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
        }

        String username = form.getFirst("username");
        String password = form.getFirst("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request"));
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String token = jwtUtils.generateToken(username);
            long expiresInSeconds = (jwtUtils.getExpirationMs() / 1000L);

            return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "token_type", "bearer",
                    "expires_in", expiresInSeconds
            ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_grant"));
        }
    }
}
