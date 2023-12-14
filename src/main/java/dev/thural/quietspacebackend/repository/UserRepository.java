package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserDTO, UUID> {
}
