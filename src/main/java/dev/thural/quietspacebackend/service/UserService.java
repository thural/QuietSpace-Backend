package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<UserDTO> getAll();

    UserDTO addOne(UserDTO user);

    Optional<UserDTO> getById(UUID id);

    void updateOne(UUID id, UserDTO user);

    void deleteOne(UUID id);

    void patchOne(UUID id, UserDTO user);
}
