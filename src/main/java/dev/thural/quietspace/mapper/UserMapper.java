package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User userRequestToEntity(UserRequest userRequest);

    UserResponse userEntityToResponse(User user);
    UserRequest userEntityToRequest(User user);

}
