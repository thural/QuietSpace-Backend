package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserDTO> listUsers(String username, Integer pageNumber, Integer pageSize);

    Page<UserDTO> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    Optional<UserDTO> getUserById(UUID id);

    Optional<UserDTO> updateUser(UserEntity loggedUser, UserDTO user);

    Boolean deleteUser(UUID userId, String authHeader);

    void patchUser(UserDTO userDTO, String authHeader);

    Optional<UserEntity> findUserByJwt(String authHeader);

    Optional<UserDTO> findUserDtoByJwt(String authHeader);
}
