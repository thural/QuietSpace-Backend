package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDto;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserDto> listUsers(String username, Integer pageNumber, Integer pageSize);

    Page<UserDto> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    Optional<UserDto> getUserById(UUID id);

    Optional<UserDto> updateUser(UserEntity loggedUser, UserDto user);

    Boolean deleteUser(UUID userId, String authHeader);

    void patchUser(UserDto userDTO, String authHeader);

    Optional<UserEntity> findUserByJwt(String authHeader);

    Optional<UserDto> findUserDtoByJwt(String authHeader);
}
