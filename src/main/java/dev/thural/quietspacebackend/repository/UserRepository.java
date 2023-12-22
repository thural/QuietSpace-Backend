package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Page<UserEntity> findAllByUsernameIsLikeIgnoreCase(String userName, Pageable pageable);

    Optional<UserEntity> findUserEntityByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.username LIKE %:query% OR u.email LIKE %:query%")
    Page<UserEntity> findAllByQuery(String query, PageRequest pageRequest);
}
