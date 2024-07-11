package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.utils.enums.RoleType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .role(RoleType.USER.toString())
            .username("user")
            .firstname("firstname")
            .lastname("lastname")
            .password("78921731")
            .accountLocked(false)
            .username("test user")
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private User savedUser;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
    }

    @Test
    void testGetUserListByName() {
        Page<User> list = userRepository.findAllByUsernameIsLikeIgnoreCase("%user%", null);
        assertThat(list.toList().size()).isEqualTo(1);
        assertThat(list.toList().get(0).getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void getUserById() {
        User foundUser = userRepository.findById(savedUser.getId())
                .orElse(null);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void testFindUserByUsername() {
        User userEntity = userRepository.findUserByUsername(user.getUsername())
                .orElse(null);

        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(savedUser.getId());
        assertThat(userEntity.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void testFindUserEntityByEmail() {
        User userEntity = userRepository.findUserEntityByEmail(user.getEmail())
                .orElse(null);

        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(savedUser.getId());
        assertThat(userEntity.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testSavedUserNameIsTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
            userRepository.save(User.builder()
                    .username("test user random text longer than 32 characters")
                    .build());

            userRepository.flush();
        });
    }
}