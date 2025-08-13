package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateQuestionDTO;
import com.example.studybuddy.model.Question;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.QuestionRepository;
import com.example.studybuddy.repository.QuizRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public List<Question> findAllByQuiz(Long quizId) {
        quizRepository.findById(quizId).orElseThrow(() ->
                new EntityNotFoundException("Quiz not found with id " + quizId));
        return questionRepository.findAllByQuizId(quizId);
    }

    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id " + id));
    }

    public Question createQuestion(Long quizId, CreateQuestionDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id " + quizId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) username = ((UserDetails) principal).getUsername();
        else username = principal.toString();

        User owner = quiz.getCourse().getOwner();
        if (!isAdmin && (owner == null || !username.equals(owner.getUsername()))) {
            throw new AccessDeniedException("Only the course owner or an admin can add questions");
        }

        int optionsSize = dto.getOptions().size();
        for (Integer idx : dto.getCorrectAnswers()) {
            if (idx == null || idx < 0 || idx >= optionsSize) {
                throw new IllegalArgumentException("correctAnswers contains invalid index: " + idx);
            }
        }

        Question q = new Question();
        q.setText(dto.getText());
        q.setOptions(dto.getOptions());
        q.setCorrectAnswers(dto.getCorrectAnswers());
        q.setQuiz(quiz);

        return questionRepository.save(q);
    }

    public Question updateQuestion(Long quizId, Long questionId, CreateQuestionDTO dto) {
        Question existing = findById(questionId);
        if (!existing.getQuiz().getId().equals(quizId)) {
            throw new IllegalArgumentException("Question does not belong to quiz " + quizId);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) username = ((UserDetails) principal).getUsername();
        else username = principal.toString();
        User owner = existing.getQuiz().getCourse().getOwner();
        if (!isAdmin && (owner == null || !username.equals(owner.getUsername()))) {
            throw new AccessDeniedException("Only the course owner or an admin can update questions");
        }

        int optionsSize = dto.getOptions().size();
        for (Integer idx : dto.getCorrectAnswers()) {
            if (idx == null || idx < 0 || idx >= optionsSize) {
                throw new IllegalArgumentException("correctAnswers contains invalid index: " + idx);
            }
        }

        existing.setText(dto.getText());
        existing.setOptions(dto.getOptions());
        existing.setCorrectAnswers(dto.getCorrectAnswers());
        return questionRepository.save(existing);
    }

    public void deleteById(Long quizId, Long questionId) {
        Question q = findById(questionId);
        if (!q.getQuiz().getId().equals(quizId)) {
            throw new IllegalArgumentException("Question does not belong to quiz " + quizId);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) username = ((UserDetails) principal).getUsername();
        else username = principal.toString();
        User owner = q.getQuiz().getCourse().getOwner();
        if (!isAdmin && (owner == null || !username.equals(owner.getUsername()))) {
            throw new AccessDeniedException("Only the course owner or an admin can delete questions");
        }

        questionRepository.deleteById(questionId);
    }
}