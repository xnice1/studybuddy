package com.example.studybuddy.controller;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.dto.CreateQuizDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.service.QuizService;
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
@RequestMapping("/api/quizzes")
@Tag(name = "Quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    @Operation(summary = "List quizzes for a course")
    public ResponseEntity<List<QuizDTO>> findAll() {
        List<QuizDTO> dtos = quizService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{quizId}")
    @Operation(summary = "Get a quiz by ID")
    public ResponseEntity<QuizDTO> findById(@PathVariable Long quizId) {
        Quiz quiz = quizService.findById(quizId);
        return ResponseEntity.ok(toDTO(quiz));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping
    @Operation(summary = "Create a quiz", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<QuizDTO> create(@RequestBody @Valid CreateQuizDTO dto) {
        Quiz saved = quizService.createQuiz(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{quizId}")
    @Operation(summary = "Update a quiz", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<QuizDTO> update(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizDTO dto
    ) {
        Quiz updated = quizService.update(quizId, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{quizId}")
    @Operation(summary = "Delete a quiz", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long quizId) {
        quizService.deleteById(quizId);
        return ResponseEntity.noContent().build();
    }

    private QuizDTO toDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setCourseId(quiz.getCourse().getId());
        return dto;
    }

    private Quiz fromDTO(QuizDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        Course course = new Course();
        course.setId(dto.getCourseId());
        quiz.setCourse(course);
        return quiz;
    }
}

