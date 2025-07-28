package com.example.studybuddy.controller;

import com.example.studybuddy.dto.QuestionDTO;
import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
@Tag(name = "Questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    @Operation(summary = "List questions for a quiz")
    public ResponseEntity<List<QuestionDTO>> findAll() {
        List<QuestionDTO> dtos = questionService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a question by ID")
    public ResponseEntity<QuestionDTO> findById(@PathVariable Long id) {
        Question question = questionService.findById(id);
        return ResponseEntity.ok(toDTO(question));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Add a question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<QuestionDTO> create(@RequestBody @Valid QuestionDTO dto) {
        Question saved = questionService.save(fromDTO(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{questionId}")
    @Operation(summary = "Update a question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<QuestionDTO> update(
            @PathVariable Long questionId,
            @RequestBody @Valid QuestionDTO dto
    ) {
        Question updated = questionService.update(questionId, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{questionId}")
    @Operation(summary = "Delete a question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long questionId) {
        questionService.deleteById(questionId);
        return ResponseEntity.noContent().build();
    }

    private QuestionDTO toDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setText(q.getText());
        dto.setOptions(q.getOptions());
        dto.setCorrectAnswers(q.getCorrectAnswers());
        dto.setQuizId(q.getQuiz().getId());
        return dto;
    }

    private Question fromDTO(QuestionDTO dto) {
        Question q = new Question();
        q.setText(dto.getText());
        q.setOptions(dto.getOptions());
        q.setCorrectAnswers(dto.getCorrectAnswers());
        Quiz quiz = new Quiz();
        quiz.setId(dto.getQuizId());
        q.setQuiz(quiz);
        return q;
    }
}
