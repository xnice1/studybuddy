package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import com.example.studybuddy.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> findAll() {
        List<CourseDTO> dtos = courseService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> findById(@PathVariable Long id) {
        Course course = courseService.findById(id);
        return ResponseEntity.ok(toDTO(course));
    }

    @PostMapping
    public ResponseEntity<CourseDTO> create(@RequestBody @Valid CourseDTO dto) {
        Course saved = courseService.save(fromDTO(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CourseDTO dto
    ) {
        Course updated = courseService.update(id, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
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

