package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateQuizDTO;
import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.QuizRepository;
import com.example.studybuddy.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;



@Service
@Transactional
public class QuizService {
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;

    public QuizService(QuizRepository quizRepository, CourseRepository courseRepository) {
        this.quizRepository = quizRepository;
        this.courseRepository = courseRepository;
    }

    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    public Quiz findById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id " + id));
    }


    public Quiz save(Quiz quiz) {
        Long courseId = quiz.getCourse() != null ? quiz.getCourse().getId() : null;
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id " + courseId));
        quiz.setCourse(course);
        return quizRepository.save(quiz);
    }

    public Quiz createQuiz(CreateQuizDTO dto) {
        Long courseId = dto.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id " + courseId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User owner = course.getOwner();
        if (!isAdmin && (owner == null || !username.equals(owner.getUsername()))) {
            throw new AccessDeniedException("Only the course owner or an admin can create a quiz for this course");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setCourse(course);
        return quizRepository.save(quiz);
    }

    public Quiz update(Long id, Quiz updated) {
        Quiz existing = findById(id);
        existing.setTitle(updated.getTitle());
        if (updated.getCourse() != null) {
            Long newCourseId = updated.getCourse().getId();
            Course newCourse = courseRepository.findById(newCourseId)
                    .orElseThrow(() -> new EntityNotFoundException("Course not found with id " + newCourseId));
            existing.setCourse(newCourse);
        }
        return quizRepository.save(existing);
    }

    public void deleteById(Long id) {
        quizRepository.deleteById(id);
    }
}
