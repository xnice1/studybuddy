package com.example.studybuddy.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String grant_type;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGrant_type() { return grant_type; }
    public void setGrant_type(String grant_type) { this.grant_type = grant_type; }
}
