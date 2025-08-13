package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CreateQuestionDTO;
import com.example.studybuddy.dto.QuestionDTO;
import com.example.studybuddy.model.Question;
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
    public ResponseEntity<List<QuestionDTO>> findAll(@PathVariable Long quizId) {
        List<QuestionDTO> dtos = questionService.findAllByQuiz(quizId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "Get a question by ID")
    public ResponseEntity<QuestionDTO> findById(@PathVariable Long quizId, @PathVariable Long questionId) {
        Question question = questionService.findById(questionId);
        if (!question.getQuiz().getId().equals(quizId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(toDTO(question));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping
    @Operation(summary = "Add a question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<QuestionDTO> create(@PathVariable Long quizId, @RequestBody @Valid CreateQuestionDTO dto) {
        Question saved = questionService.createQuestion(quizId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/{questionId}")
    @Operation(summary = "Update a question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<QuestionDTO> update(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestBody @Valid CreateQuestionDTO dto
    ) {
        Question updated = questionService.updateQuestion(quizId, questionId, dto);
        return ResponseEntity.ok(toDTO(updated));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @DeleteMapping("/{questionId}")
    @Operation(summary = "Delete a question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long quizId, @PathVariable Long questionId) {
        questionService.deleteById(quizId, questionId);
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
}
