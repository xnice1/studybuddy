package com.example.studybuddy.mapper;

import com.example.studybuddy.dto.CourseDTO;
import com.example.studybuddy.model.Course;
import com.example.studybuddy.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface CourseMapper {


    @Mapping(target ="owner",source ="ownerId",qualifiedByName = "idToOwner")
    Course toEntity(CourseDTO dto);

    @Mapping(target = "ownerId", source = "owner.id")
    CourseDTO toDto(Course course);

    @Mapping(target = "owner", source = "ownerId")
    @Mapping(target = "id", source = "id")
    Course fromDto(CourseDTO dto);

    @Named("idToOwner")
        default User idToOwner(Long id) {
        if (id == null) return null;
        User c = new User();
        c.setId(id);
        return c;
    }
}
