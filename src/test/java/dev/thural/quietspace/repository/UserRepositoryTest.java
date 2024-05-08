package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;


import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void testGetUserListByName(){
        Page<User> list = userRepository.findAllByUsernameIsLikeIgnoreCase("%John%", null);

        assertThat(list.toList().size()).isEqualTo(33);
    }

    @Test
    void testSavedUser(){
        User savedUser = userRepository.save(User.builder()
                .username("test user")
                .build());

        userRepository.flush();

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void getUserById() {
        UUID userId = UUID.fromString("e18d0c0c-37a4-4e50-8041-bd49ffde8182");
        userRepository.findById(userId);

        userRepository.flush();

        verify(userRepository).findById(any(UUID.class));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testSavedUserNameIsTooLong(){
        assertThrows(ConstraintViolationException.class, () -> {
            userRepository.save(User.builder()
                    .username("test user random text longer than 32 characters")
                    .build());

            userRepository.flush();
        });
    }
}