package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.model.request.LoginRequest;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserDTO> listUsers(String username, Integer pageNumber, Integer pageSize);

    AuthResponse addOne(UserDTO user);

    Optional<UserDTO> getById(UUID id);

    Optional<UserDTO> updateOne(UserEntity loggedUser, UserDTO user);

    Boolean deleteOne(UUID userId, String authHeader);

    void patchOne(UserDTO userDTO, String authHeader);

    Page<UserDTO> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    AuthResponse getByLoginRequest(LoginRequest loginRequest);

    Optional<UserEntity> findUserByJwt(String authHeader);

    Optional<UserDTO> findUserDtoByJwt(String authHeader);

}
