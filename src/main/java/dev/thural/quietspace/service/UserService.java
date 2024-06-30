package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Page<UserResponse> listUsers(String username, Integer pageNumber, Integer pageSize);

    Page<UserResponse> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    List<User> getUsersFromIdList(List<UUID> userIds);

    Optional<UserResponse> getUserResponseById(UUID id);

    Optional<User> getUserById(UUID userId);

    void deleteUser(UUID userId, String authHeader);

    User createUser(UserRegisterRequest userRegisterRequest);

    UserResponse patchUser(UserRegisterRequest userRegisterRequest);

    Optional<UserResponse> getLoggedUserResponse();

    User getLoggedUser();

}
