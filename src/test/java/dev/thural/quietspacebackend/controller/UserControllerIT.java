package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserControllerIT {

    @Autowired
    UserController userController;

    @Autowired
    UserRepository userRepository;

    UserMapper userMapper;

    @Rollback
    @Transactional
    @Test
    void testGetAllUsers() {
        userRepository.deleteAll();
        List<UserDTO> userList = userController.getAllUsers();
        assertThat(userList.size()).isEqualTo(8);

    }

    @Test
    void testGetById() {
        UserEntity userEntity = userRepository.findAll().get(0);

        UserDTO userDTO = userController.getUserById(userEntity.getId());

        assertThat(userDTO).isNotNull();
    }

    @Test
    void testUserNotFound(){
        assertThrows(NotFoundException.class, () -> {
            userController.getUserById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testCreateUser(){
        UserDTO userDTO = UserDTO.builder()
                .username("new test user")
                .build();

        ResponseEntity response = userController.createUser(userDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(response.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = response.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[8]);

        UserEntity userEntity = userRepository.findById(savedUUID).orElse(null);
        assertThat(userEntity).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingUser(){
        UserEntity userEntity = userRepository.findAll().get(0);
        UserDTO userDTO =  userMapper.userEntityToDto(userEntity);
        final String updatedName = "updated user name";
        userDTO.setUsername(updatedName);

        ResponseEntity response = userController.putUser(userEntity.getId(), userDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        UserEntity updatedUser = userRepository.findById(userEntity.getId()).orElse(null);
        assertThat(updatedUser.getUsername()).isEqualTo(updatedName);

    }

}