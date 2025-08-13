package com.example.studybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateQuizDTO {

    @NotBlank(message = "Quiz title is required")
    @Size(max = 255, message = "Quiz title must be at most 255 characters")
    private String title;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}
