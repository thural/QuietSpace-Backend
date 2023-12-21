package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Page<UserEntity> findAllByUsernameIsLikeIgnoreCase(String userName, Pageable pageable);
}
