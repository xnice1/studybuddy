package com.example.studybuddy.service;

import com.example.studybuddy.model.Question;
import com.example.studybuddy.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.repository.QuizRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id " + id));
    }

    public Question save(Question question) {
        Long quizId = question.getQuiz() != null ? question.getQuiz().getId() : null;
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id " + quizId));
        if (question.getCorrectAnswers().stream().anyMatch(idx -> idx < 0 || idx >= question.getOptions().size())) {
            throw new IllegalArgumentException("Invalid correct answer index");
        }
        question.setQuiz(quiz);
        return questionRepository.save(question);
    }

    public Question update(Long id, Question updated) {
        Question existing = findById(id);
        existing.setText(updated.getText());
        existing.setOptions(updated.getOptions());
        existing.setCorrectAnswers(updated.getCorrectAnswers());
        if (updated.getQuiz() != null) {
            Long newQuizId = updated.getQuiz().getId();
            Quiz newQuiz = quizRepository.findById(newQuizId)
                    .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id " + newQuizId));
            existing.setQuiz(newQuiz);
        }
        return questionRepository.save(existing);
    }

    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }
}