package com.example.studybuddy.controller;

import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.User;
import com.example.studybuddy.service.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<Question>> findAll() {
        List<Question> questions = questionService.findAll();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> findById(@PathVariable Long id) {
        Question question = questionService.findById(id);
        return ResponseEntity.ok(question);
    }

    @PostMapping
    public ResponseEntity<Question> create(@RequestBody Question question) {
        Question saved = questionService.save(question);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> update(
            @PathVariable Long id,
            @RequestBody Question question
    ) {
        Question updated = questionService.update(id, question);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public void handleNotFound() {}
}
