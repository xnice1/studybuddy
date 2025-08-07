package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.CourseRepository;
import com.example.studybuddy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User instructor;

    @BeforeEach
    void setUp() {
        courseRepo.deleteAll();
        userRepo.deleteAll();

        User adminUser = new User();
        adminUser.setUsername("superadmin");
        adminUser.setPassword("irrelevant");
        adminUser.setRole("ADMIN");
        userRepo.save(adminUser);

        instructor = new User();
        instructor.setUsername("inst1");
        instructor.setPassword("irrelevant");
        instructor.setRole("INSTRUCTOR");
        userRepo.save(instructor);

        Course c1 = new Course();
        c1.setTitle("Math 101");
        c1.setDescription("Basic Math");
        c1.setOwner(instructor);
        courseRepo.save(c1);

        Course c2 = new Course();
        c2.setTitle("Physics");
        c2.setDescription("Intro to Physics");
        c2.setOwner(instructor);
        courseRepo.save(c2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_returnsTwo() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Math 101"))
                .andExpect(jsonPath("$[1].title").value("Physics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_notFound() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", 12345L))
                .andExpect(status().isNotFound());
    }
    void anyEndpoint_unauthenticated_getsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_returnsCorrectCourse() throws Exception {
        Long id = courseRepo.findAll().get(0).getId();

        mockMvc.perform(get("/api/courses/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Math 101"))
                .andExpect(jsonPath("$.ownerId").value(instructor.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_adminSucceeds() throws Exception {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Chemistry");
        dto.setDescription("Mendeleev’s wonders");
        dto.setOwnerId(instructor.getId());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Chemistry"))
                .andExpect(jsonPath("$.ownerId").value(instructor.getId()));

        assertThat(courseRepo.count()).isEqualTo(3);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_instructorForbidden() throws Exception {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Biology");
        dto.setDescription("Life sciences");
        dto.setOwnerId(instructor.getId());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_missingTitle_badRequest() throws Exception {
        CourseDTO dto = new CourseDTO();
        dto.setDescription("No title");
        dto.setOwnerId(instructor.getId());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_ownerNotFound_notFound() throws Exception {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Ghost Course");
        dto.setDescription("Owner missing");
        dto.setOwnerId(9999L);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourse_adminSucceeds() throws Exception {
        Long id = courseRepo.findAll().get(0).getId();

        CourseDTO dto = new CourseDTO();
        dto.setTitle("Math 201");
        dto.setDescription("Advanced Math");
        dto.setOwnerId(instructor.getId());

        mockMvc.perform(put("/api/courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Math 201"))
                .andExpect(jsonPath("$.description").value("Advanced Math"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateCourse_studentForbidden() throws Exception {
        Long id = courseRepo.findAll().get(0).getId();

        CourseDTO dto = new CourseDTO();
        dto.setTitle("Math 301");
        dto.setDescription("Impossible Math");
        dto.setOwnerId(instructor.getId());

        mockMvc.perform(put("/api/courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourse_adminSucceeds() throws Exception {
        Long id = courseRepo.findAll().get(0).getId();

        mockMvc.perform(delete("/api/courses/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(courseRepo.existsById(id)).isFalse();
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void deleteCourse_instructorForbidden() throws Exception {
        Long id = courseRepo.findAll().get(0).getId();

        mockMvc.perform(delete("/api/courses/{id}", id))
                .andExpect(status().isForbidden());
    }
}