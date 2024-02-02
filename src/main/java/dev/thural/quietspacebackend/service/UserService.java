package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDto;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserDto> listUsers(String username, Integer pageNumber, Integer pageSize);

    Page<UserDto> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    Optional<UserDto> getUserById(UUID id);

    Boolean deleteUser(UUID userId, String authHeader);

    void patchUser(UserDto userDTO);

    Optional<UserDto> findLoggedUser();
}
