package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    boolean existsByJwtToken(String jwtToken);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    Token getByEmail(String email);
}
