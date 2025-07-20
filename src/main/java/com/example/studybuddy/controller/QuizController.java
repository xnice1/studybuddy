package com.example.studybuddy.controller;

import com.example.studybuddy.dto.QuizDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<QuizDTO>> findAll() {
        List<QuizDTO> dtos = quizService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDTO> findById(@PathVariable Long id) {
        Quiz quiz = quizService.findById(id);
        return ResponseEntity.ok(toDTO(quiz));
    }

    @PostMapping
    public ResponseEntity<QuizDTO> create(@RequestBody @Valid QuizDTO dto) {
        Quiz saved = quizService.save(fromDTO(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid QuizDTO dto
    ) {
        Quiz updated = quizService.update(id, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.deleteById(id);
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

