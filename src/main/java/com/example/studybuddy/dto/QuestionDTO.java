package com.example.studybuddy.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class QuestionDTO {

    private Long id;

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must be at most 1000 characters")
    private String text;

    @NotEmpty(message = "At least one option is required")
    private List<@NotBlank(message = "Option text cannot be blank")
    @Size(max = 255, message = "Option must be at most 255 characters")
            String> options;

    @NotEmpty(message = "At least one correct answer index is required")
    private List<@NotNull(message = "Answer index cannot be null") Integer> correctAnswers;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<Integer> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<Integer> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

}
