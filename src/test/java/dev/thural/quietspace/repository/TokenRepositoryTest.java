package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TokenRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .username("user")
            .firstname("firstname")
            .lastname("lastname")
            .password("78921731")
            .role(Role.USER)
            .accountLocked(false)
            .username("test user")
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private Token token = Token.builder()
            .token("32r33fg32fg22d33f4g42fg3")
            .user(user)
            .createDate(OffsetDateTime.now())
            .email("user@gmail.com")
            .build();

    private User savedUser;
    private Token savedToken;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user);
        this.savedToken = tokenRepository.save(token);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
        tokenRepository.delete(token);
    }

    @Test
    void existsByToken() {
        boolean isExists = tokenRepository.existsByToken(token.getToken());
        assertTrue(isExists);
    }

    @Test
    void existsByEmail() {
        boolean isExists = tokenRepository.existsByEmail(token.getEmail());
        assertTrue(isExists);
    }

    @Test
    void deleteByEmail() {
        tokenRepository.deleteByEmail(user.getEmail());
        boolean isExists = tokenRepository.existsByEmail(user.getEmail());
        assertFalse(isExists);
    }

    @Test
    void getByEmail() {
        Optional<Token> foundToken = tokenRepository.getByEmail(token.getEmail());
        assertTrue(foundToken.isPresent());
        assertEquals(token.getToken(), foundToken.get().getToken());
    }

    @Test
    void findByToken() {
        Optional<Token> foundToken = tokenRepository.findByToken(token.getToken());
        assertTrue(foundToken.isPresent());
        assertEquals(token.getToken(), foundToken.get().getToken());
    }
}