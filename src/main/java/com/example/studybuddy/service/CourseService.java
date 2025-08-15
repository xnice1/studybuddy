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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

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
            username = String.valueOf(principal);
        }

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setOwner(owner);

        return courseRepository.save(course);
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }

    public void ensureOwnerOrAdmin(Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        boolean admin = isAdmin();
        if (admin) return;

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) username = ((UserDetails) principal).getUsername();
        else username = String.valueOf(principal);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id " + courseId));

        if (course.getOwner() == null || course.getOwner().getUsername() == null ||
                !username.equals(course.getOwner().getUsername())) {
            throw new AccessDeniedException("Only the course owner or an admin can perform this action");
        }
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
            username = String.valueOf(principal);
        }

        return courseRepository.findAll().stream()
                .filter(c -> c.getOwner() != null && username.equals(c.getOwner().getUsername()))
                .collect(Collectors.toList());
    }

    public Course update(Long id, Course updated) {
        ensureOwnerOrAdmin(id);

        Course existing = findById(id);

        if (updated.getTitle() != null) {
            existing.setTitle(updated.getTitle());
        }
        existing.setDescription(updated.getDescription());

        if (updated.getOwner() != null && updated.getOwner().getId() != null) {
            if (!isAdmin()) {
                throw new AccessDeniedException("Only admin may change course owner");
            }
            Long newOwnerId = updated.getOwner().getId();
            User newOwner = userRepository.findById(newOwnerId)
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found with id " + newOwnerId));
            existing.setOwner(newOwner);
        }

        return courseRepository.save(existing);
    }


    public Course updateFromDto(Long id, CreateCourseDTO dto) {
        ensureOwnerOrAdmin(id);

        Course existing = findById(id);
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());

        if (dto.getOwnerId() != null) {
            if (!isAdmin()) {
                throw new AccessDeniedException("Only admin may change course owner");
            }
            User newOwner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found with id " + dto.getOwnerId()));
            existing.setOwner(newOwner);
        }

        return courseRepository.save(existing);
    }

    public void deleteById(Long id) {
        ensureOwnerOrAdmin(id);
        courseRepository.deleteById(id);
    }
}
