package com.example.studybuddy.repository;

import com.example.studybuddy.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOwnerUsername(String username);
    boolean existsByOwnerId(Long ownerId);
}
