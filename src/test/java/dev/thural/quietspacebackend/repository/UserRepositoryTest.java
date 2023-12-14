package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void testSavedUser(){
        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .username("test user")
                .build());
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }
}