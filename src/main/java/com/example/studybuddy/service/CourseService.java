package com.example.studybuddy.service;

import com.example.studybuddy.model.Course;
import com.example.studybuddy.repository.CourseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id " + id));
    }


    public Course save(Course course) {
        Long ownerId = course.getOwner() != null ? course.getOwner().getId() : null;
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with id " + ownerId));
        course.setOwner(owner);
        return courseRepository.save(course);
    }

    public Course update(Long id, Course updated) {
        Course existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        if (updated.getOwner() != null) {
            Long newOwnerId = updated.getOwner().getId();
            User newOwner = userRepository.findById(newOwnerId)
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found with id " + newOwnerId));
            existing.setOwner(newOwner);
        }
        return courseRepository.save(existing);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}