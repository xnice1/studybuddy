package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.dto.CreateCourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    @Operation(summary = "List all courses (admin=all, user=their courses)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CourseDTO>> findAll() {
        List<CourseDTO> dtos = courseService.findAllForCurrentUser().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurity.isCourseOwner(principal.username, #id)")
    @Operation(summary = "Get course by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseDTO> findById(@PathVariable Long id) {
        Course course = courseService.findById(id);
        return ResponseEntity.ok(toDTO(course));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(summary = "Create a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseDTO> create(@Valid @RequestBody CreateCourseDTO dto) {
        Course saved = courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurity.isCourseOwner(principal.username, #id)")
    @Operation(summary = "Update a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCourseDTO dto
    ) {
        Course updated = courseService.updateFromDto(id, dto);
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurity.isCourseOwner(principal.username, #id)")
    @Operation(summary = "Delete a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CourseDTO toDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setOwnerId(course.getOwner().getId());
        return dto;
    }

}
