package com.example.studybuddy.controller;

import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> findAll() {
        List<Quiz> quiz = quizService.findAll();
        return ResponseEntity.ok(quiz);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> findById(@PathVariable Long id) {
        Quiz quiz = quizService.findById(id);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping
    public ResponseEntity<Quiz> create(@RequestBody Quiz quiz) {
        Quiz saved = quizService.save(quiz);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> update(
            @PathVariable Long id,
            @RequestBody Quiz quiz
    ) {
        Quiz updated = quizService.update(id, quiz);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public void handleNotFound() {}
}
