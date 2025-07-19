package com.example.studybuddy.service;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    public User save(User user) {
        return userRepository.save(user);
    }
    public User update(Long id, User updated) {
        User existing = findById(id);
        existing.setUsername(updated.getUsername());
        existing.setPassword(updated.getPassword());
        existing.setRole(updated.getRole());
        return userRepository.save(existing);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}