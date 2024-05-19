package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Page<UserResponse> listUsers(String username, Integer pageNumber, Integer pageSize);

    Page<UserResponse> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    List<User> getUsersFromIdList(List<UUID> userIds);

    Optional<UserResponse> getUserById(UUID id);

    void deleteUser(UUID userId, String authHeader);

    void patchUser(UserRequest userRequest);

    Optional<UserResponse> getLoggedUserResponse();

    Optional<User> getLoggedUser();

}
