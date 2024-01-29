package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDto;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerIT {

    @Autowired
    UserController userController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthService authService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void createUserInvalidUserName() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDto userDTO = UserDto.builder()
                .id(userId)
                .username("a long text for username which should exceed 32 character limit")
                .build();

        AuthResponse authResponse = new AuthResponse("tokenTokenToken", "user created", "7817398717");

        given(authService.register(any(UserDto.class))).willReturn(authResponse);

        MvcResult result = mockMvc.perform(post(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1))).andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void testListUserByNamePage1() throws Exception {
        mockMvc.perform(get(UserController.USER_PATH)
                        .queryParam("userName", "John")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(25)))
                .andExpect(jsonPath("$.[0].userName", is("John")));
    }

    @Rollback
    @Transactional
    @Test
    void testGetAllUsers() {
        userRepository.deleteAll();
        Page<UserDto> userList = userController.listUsers(null, 1, 25);
        assertThat(userList.getContent().size()).isEqualTo(8);

    }

    @Test
    void testGetById() {
        UserEntity userEntity = userRepository.findAll().get(0);

        UserDto userDto = userController.getUserById(userEntity.getId());

        assertThat(userDto).isNotNull();
    }

    @Test
    void testUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            userController.getUserById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testCreateUser() {
        UserDto userDto = UserDto.builder()
                .username("new test user")
                .build();

        ResponseEntity<?> response = userController.putUser("auth header", userDto);

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
    void testUpdateExistingUser() {
        UserEntity userEntity = userRepository.findAll().get(0);
        UserDto userDto = userMapper.userEntityToDto(userEntity);
        final String updatedName = "updated user name";
        userDto.setUsername(updatedName);

        ResponseEntity<?> response = userController.putUser(userEntity.getId().toString(), userDto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        UserEntity updatedUser = userRepository.findById(userEntity.getId()).orElse(null);
        assert updatedUser != null;
        assertThat(updatedUser.getUsername()).isEqualTo(updatedName);
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            userController.putUser(UUID.randomUUID().toString(), UserDto.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteUser() {
        UserEntity userEntity = userRepository.findAll().get(0);

        ResponseEntity<?> response = userController.deleteUser("auth header", userEntity.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        assertThat(userRepository.findById(userEntity.getId())).isEmpty();
    }

    @Transactional
    @Rollback
    @Test
    void testDeleteUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            userController.deleteUser("authentication header", UUID.randomUUID());
        });
    }

    @Test
    void testListUsersByName() throws Exception {
        mockMvc.perform(get(UserController.USER_PATH)
                        .queryParam("userName", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(100)));
    }

}