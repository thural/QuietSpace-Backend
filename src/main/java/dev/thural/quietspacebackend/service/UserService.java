package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserResponse> listUsers(String username, Integer pageNumber, Integer pageSize);

    Page<UserResponse> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    Optional<UserResponse> getUserById(UUID id);

    Boolean deleteUser(UUID userId, String authHeader);

    void patchUser(UserResponse userResponse);

    Optional<UserResponse> findLoggedUser();
}
