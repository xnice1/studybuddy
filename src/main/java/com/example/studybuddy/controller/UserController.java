package com.example.studybuddy.controller;

import com.example.studybuddy.dto.UserDTO;
import com.example.studybuddy.model.User;
import com.example.studybuddy.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody @Valid UserDTO dto) {
        User saved = userService.save(fromDTO(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(toDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        List<UserDTO> dtos = userService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(toDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid UserDTO dto
    ) {
        User updated = userService.update(id, fromDTO(dto));
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public void handleNotFound() {
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setRole(u.getRole());
        return dto;
    }

    private User fromDTO(UserDTO dto) {
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(dto.getPassword());
        u.setRole(dto.getRole());
        return u;
    }
}
