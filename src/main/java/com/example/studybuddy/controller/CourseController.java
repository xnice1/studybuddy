package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
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
    @Operation(summary = "List all courses")
    public ResponseEntity<List<CourseDTO>> findAll() {
        List<CourseDTO> dtos = courseService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<CourseDTO> findById(@PathVariable Long id) {
        Course course = courseService.findById(id);
        return ResponseEntity.ok(toDTO(course));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseDTO> create(@RequestBody @Valid CourseDTO dto) {
        Course saved = courseService.save(fromDTO(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CourseDTO dto
    ) {
        Course updated = courseService.update(id, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
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

    private Course fromDTO(CourseDTO dto) {
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        User owner = new User();
        owner.setId(dto.getOwnerId());
        course.setOwner(owner);
        return course;
    }
}

