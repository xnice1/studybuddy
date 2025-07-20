package com.example.studybuddy.controller;

import com.example.studybuddy.dto.QuestionDTO;
import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> findAll() {
        List<QuestionDTO> dtos = questionService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> findById(@PathVariable Long id) {
        Question question = questionService.findById(id);
        return ResponseEntity.ok(toDTO(question));
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> create(@RequestBody @Valid QuestionDTO dto) {
        Question saved = questionService.save(fromDTO(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid QuestionDTO dto
    ) {
        Question updated = questionService.update(id, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.deleteById(id);
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
