package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.User;
import dev.thural.quietspacebackend.model.request.UserRequest;
import dev.thural.quietspacebackend.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User userRequestToEntity(UserRequest userRequest);

    @Mapping(target = "password", ignore = true)
    UserResponse userEntityToResponse(User user);
}
