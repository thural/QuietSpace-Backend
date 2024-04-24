package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Page<User> findAllByUsernameIsLikeIgnoreCase(String userName, Pageable pageable);

    Optional<User> findUserEntityByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:query% OR u.email LIKE %:query%")
    Page<User> findAllByQuery(String query, PageRequest pageRequest);

    Optional<User> findUserByUsername(String username);
}
