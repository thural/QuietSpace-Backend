package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.response.AuthResponse;
import dev.thural.quietspacebackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class UserControllerIT {

    @Autowired
    UserController userController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

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
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .username("a long text for username which should exceed 32 character limit")
                .build();

        AuthResponse authResponse = new AuthResponse("tokenTokenToken", "user created", "7817398717");

        given(userService.addOne(any(UserDTO.class))).willReturn(authResponse);

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
        Page<UserDTO> userList = userController.listUsers(null, 1, 25);
        assertThat(userList.getContent().size()).isEqualTo(8);

    }

    @Test
    void testGetById() {
        UserEntity userEntity = userRepository.findAll().get(0);

        UserDTO userDTO = userController.getUserById(userEntity.getId());

        assertThat(userDTO).isNotNull();
    }

    @Test
    void testUserNotFound() {
        assertThrows(NotFoundException.class, () -> {
            userController.getUserById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testCreateUser() {
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
    void testUpdateExistingUser() {
        UserEntity userEntity = userRepository.findAll().get(0);
        UserDTO userDTO = userMapper.userEntityToDto(userEntity);
        final String updatedName = "updated user name";
        userDTO.setUsername(updatedName);

        ResponseEntity response = userController.putUser(userEntity.getId().toString(), userDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        UserEntity updatedUser = userRepository.findById(userEntity.getId()).orElse(null);
        assertThat(updatedUser.getUsername()).isEqualTo(updatedName);

    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> {
            userController.putUser(UUID.randomUUID().toString(), UserDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteUser() {
        UserEntity userEntity = userRepository.findAll().get(0);

        ResponseEntity response = userController.deleteUser(userEntity.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        assertThat(userRepository.findById(userEntity.getId())).isEmpty();
    }

    @Transactional
    @Rollback
    @Test
    void testDeleteUserNotFound() {
        assertThrows(NotFoundException.class, () -> {
            userController.deleteUser(UUID.randomUUID());
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