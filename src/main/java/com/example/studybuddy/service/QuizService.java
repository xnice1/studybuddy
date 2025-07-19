package com.example.studybuddy.service;

import com.example.studybuddy.model.Quiz;
import com.example.studybuddy.repository.QuizRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
public class QuizService {
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;

    public QuizService(QuizRepository quizRepository) {
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
