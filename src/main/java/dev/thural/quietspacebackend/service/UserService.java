package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<UserDTO> listUsers(String userName);

    UserDTO addOne(UserDTO user);

    Optional<UserDTO> getById(UUID id);

    Optional<UserDTO> updateOne(UUID id, UserDTO user);

    Boolean deleteOne(UUID id);

    void patchOne(UUID id, UserDTO user);
}
