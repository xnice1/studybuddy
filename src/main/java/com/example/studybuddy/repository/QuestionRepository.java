package com.example.studybuddy.repository;

import com.example.studybuddy.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {}
