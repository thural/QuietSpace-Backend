package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.response.UserResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        var response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setRole(user.getRole().name());
        response.setIsPrivateAccount(user.getProfileSettings().getIsPrivateAccount());
        return response;
    }

}
