package com.example.studybuddy.security;

import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component("courseSecurity")
public class CourseSecurity {
    private final CourseRepository courses;
    private final UserRepository users;

    public CourseSecurity(CourseRepository courses, UserRepository users) {
        this.courses = courses;
        this.users   = users;
    }

    public boolean isCourseOwner(String username, Long courseId) {
        return courses.findById(courseId)
                .map(c -> c.getOwner().getUsername().equals(username))
                .orElse(false);
    }

    public boolean isUserSelf(String username, Long ownerId) {
        return users.findByUsername(username)
                .map(u -> u.getId().equals(ownerId))
                .orElse(false);
    }
}
