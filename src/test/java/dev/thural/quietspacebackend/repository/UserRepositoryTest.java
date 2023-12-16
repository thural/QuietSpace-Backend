package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;


    @Test
    void testSavedUser(){
        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .username("test user")
                .build());

        userRepository.flush();

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void testSavedUserNameIsTooLong(){
        assertThrows(ConstraintViolationException.class, () -> {
            UserEntity savedUser = userRepository.save(UserEntity.builder()
                    .username("test user random text longer than 32 characters")
                    .build());

            userRepository.flush();
        });
    }
}