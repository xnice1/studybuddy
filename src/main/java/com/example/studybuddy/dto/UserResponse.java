package com.example.studybuddy.dto;

public class UserResponse {
    private Long id;
    private String username;
    private String role;

    public UserResponse(Long id, String username, String role) {
        this.id       = id;
        this.username = username;
        this.role     = role;
    }

    public UserResponse() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}