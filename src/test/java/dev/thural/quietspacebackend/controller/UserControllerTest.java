package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.response.AuthResponse;
import dev.thural.quietspacebackend.service.UserService;
import dev.thural.quietspacebackend.service.impls.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<UserDTO> userArgumentCaptor;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;
    UserService userService;
    UserService userServiceImpl;
    UserDetailsService userDetailsService;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userServiceImpl = new UserServiceImpl(passwordEncoder, userDetailsService, userMapper, userRepository );
    }

    @Test
    void getAllUsers() throws Exception {
        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);

        given(userService.listUsers(any(), any(), any())).willReturn(testUsers);

        mockMvc.perform(get(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testUsers.getContent().size())));
    }

    @Test
    void getUserById() throws Exception {
        UserDTO testUser = userServiceImpl.listUsers(null, null, null)
                .getContent().get(1);

        given(userService.getById(testUser.getId()))
                .willReturn(Optional.of(testUser));

        mockMvc.perform(get(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
    }

    @Test
    void createUser() throws Exception {
        AuthResponse authResponse = new AuthResponse("tokenTokenToken", "user created", "7817398717");

        given(userService.addOne(any(UserDTO.class))).willReturn(authResponse);

        mockMvc.perform(post(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authResponse)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void updateUser() throws Exception {
        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);

        UserDTO testUser = testUsers.getContent().get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        given(userService.updateOne(any(), any())).willReturn(Optional.of(testUser));

        mockMvc.perform(put(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).updateOne(any(UUID.class), any(UserDTO.class));
    }

    @Test
    void deleteUser() throws Exception {
        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);
        UserDTO testUser = testUsers.getContent().get(0);

        given(userService.deleteOne(any())).willReturn(true);

        mockMvc.perform(delete(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteOne(uuidArgumentCaptor.capture());

        assertThat(testUser.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void patchUser() throws Exception {
        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);

        UserDTO testUser = testUsers.getContent().get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        mockMvc.perform(patch(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).patchOne(uuidArgumentCaptor.capture(), userArgumentCaptor.capture());

        assertThat(testUser.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(testUser.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(testUser.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
    }

    @Test
    void userByIdNotFound() throws Exception {
        given(userService.getById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(UserController.USER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserNullUserName() throws Exception {
        UserDTO userDTO = UserDTO.builder().build();

        given(userService.addOne(any(UserDTO.class)))
                .willReturn(userService.listUsers(null, null, null)
                .getContent().get(0));

        MvcResult result = mockMvc.perform(post(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest()).andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

}