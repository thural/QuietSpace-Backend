package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final CommonService commonService;

    public UserResponse toResponse(User user) {
        var signedUser = commonService.getSignedUser();
        var response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setIsFollower(signedUser.getFollowers().contains(user));
        response.setIsFollowing(user.getFollowers().contains(signedUser));
        response.setRole(user.getRole().name());
        response.setIsPrivateAccount(user.getProfileSettings().getIsPrivateAccount());
        return response;
    }

    public UserResponse toProfileResponse(User user) {
        var response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setRole(user.getRole().name());
        response.setIsPrivateAccount(user.getProfileSettings().getIsPrivateAccount());
        return response;
    }

}
