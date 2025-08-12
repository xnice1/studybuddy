package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateCourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Course> findAllForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return courseRepository.findAll();
        }

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return courseRepository.findAllByOwnerUsername(username);
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id " + id));
    }


    public Course save(Course course) {
        Long ownerId = course.getOwner() != null ? course.getOwner().getId() : null;
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner id is required to save a course");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with id " + ownerId));

        course.setOwner(owner);
        return courseRepository.save(course);
    }

    public Course createCourse(CreateCourseDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
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


    public Course updateFromDto(Long id, CreateCourseDTO dto) {
        Course existing = findById(id);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        return courseRepository.save(existing);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}
