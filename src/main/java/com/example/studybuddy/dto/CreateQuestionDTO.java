package com.example.studybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateQuestionDTO {

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must be at most 1000 characters")
    private String text;

    @NotEmpty(message = "At least one option is required")
    private List<@NotBlank @Size(max = 255) String> options;

    @NotEmpty(message = "At least one correct answer index is required")
    private List<@NotNull Integer> correctAnswers;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public List<Integer> getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(List<Integer> correctAnswers) { this.correctAnswers = correctAnswers; }
}
