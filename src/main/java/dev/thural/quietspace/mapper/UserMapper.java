package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserResponse toResponse(User user);

    UserRegisterRequest toRequest(User user);

}
