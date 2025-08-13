package com.example.studybuddy.security;

import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component("courseSecurity")
@Transactional(readOnly = true)
public class CourseSecurity {
    private static final Logger log = LoggerFactory.getLogger(CourseSecurity.class);

    private final CourseRepository courses;
    private final UserRepository users;

    public CourseSecurity(CourseRepository courses, UserRepository users) {
        this.courses = courses;
        this.users = users;
    }


    public boolean isCourseOwner(String username, Long courseId) {
        if (username == null || courseId == null) {
            return false;
        }

        return courses.findById(courseId)
                .map(c -> {
                    if (c.getOwner() == null) {
                        log.debug("Course {} has no owner", courseId);
                        return false;
                    }
                    return Objects.equals(username, c.getOwner().getUsername());
                })
                .orElseGet(() -> {
                    log.debug("Course {} not found", courseId);
                    return false;
                });
    }

    public boolean isUserSelf(String username, Long ownerId) {
        if (username == null || ownerId == null) {
            return false;
        }

        return users.findByUsername(username)
                .map(u -> {
                    if (u.getId() == null) {
                        log.debug("User {} found but id is null", username);
                        return false;
                    }
                    return Objects.equals(ownerId, u.getId());
                })
                .orElseGet(() -> {
                    log.debug("User {} not found", username);
                    return false;
                });
    }
}
