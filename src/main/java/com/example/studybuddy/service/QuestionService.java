package com.example.studybuddy.service;

import com.example.studybuddy.model.Question;
import com.example.studybuddy.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    public Question save(Question question) {
        return questionRepository.save(question);
    }

    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }
}