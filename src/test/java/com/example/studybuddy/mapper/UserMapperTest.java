package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.RegistrationRequest;
import com.example.studybuddy.dto.UserResponse;
import com.example.studybuddy.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void registrationRequest_to_entity_mapsFields() {
        RegistrationRequest req = new RegistrationRequest();
        req.setUsername("john");
        req.setPassword("pw");
        req.setRole("STUDENT");

        User user = mapper.toEntity(req);

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getPassword()).isEqualTo("pw");
        assertThat(user.getRole()).isEqualTo("STUDENT");
    }

    @Test
    void entity_to_response_excludesPassword() {
        User u = new User();
        u.setId(11L);
        u.setUsername("alice");
        u.setPassword("secret");
        u.setRole("ADMIN");

        UserResponse resp = mapper.toResponse(u);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(11L);
        assertThat(resp.getUsername()).isEqualTo("alice");
        assertThat(resp.getRole()).isEqualTo("ADMIN");
    }
}
