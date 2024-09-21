package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.User;
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

    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    Page<User> findAllBySearchTerm(String searchTerm, PageRequest pageRequest);

    Optional<User> findUserByUsername(String username);

    boolean existsByUsernameIgnoreCase(String admin);
}
