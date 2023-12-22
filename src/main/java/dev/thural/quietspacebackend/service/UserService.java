package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.request.LoginRequest;
import dev.thural.quietspacebackend.response.AuthResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserDTO> listUsers(String userName, Integer pageNumber, Integer pageSize);

    AuthResponse addOne(UserDTO user);

    Optional<UserDTO> getById(UUID id);

    Optional<UserDTO> updateOne(UUID id, UserDTO user);

    Boolean deleteOne(UUID id);

    void patchOne(UUID id, UserDTO user);

    Page<UserDTO> listUsersByQuery(String query, Integer pageNumber, Integer pageSize);

    AuthResponse getByLoginRequest(LoginRequest loginRequest);
}
