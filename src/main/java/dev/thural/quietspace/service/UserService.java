package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.StatusType;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Page<UserResponse> listUsers(Integer pageNumber, Integer pageSize);

    Page<UserResponse> listUsersByUsername(String username, Integer pageNumber, Integer pageSize);

    List<User> getUsersFromIdList(List<UUID> userIds);

    Optional<UserResponse> getUserResponseById(UUID id);

    void deleteUserById(UUID userId);

    UserResponse patchUser(UserRegisterRequest userRegisterRequest);

    Optional<UserResponse> getLoggedUserResponse();

    Page<UserResponse> listFollowings(Integer pageNumber, Integer pageSize);

    Page<UserResponse> listFollowers(Integer pageNumber, Integer pageSize);

    void toggleFollow(UUID followedUserId);

    User getSignedUser();

    void removeFollower(UUID followingUserId);

    void setOnlineStatus(String userEmail, StatusType type);

    List<UserResponse> findConnectedFollowings();

    Page<UserResponse> queryUsers(String username, String firstname, String lastname, Integer pageNumber, Integer pageSize);

    Optional<User> getUserById(UUID memberId);
}
