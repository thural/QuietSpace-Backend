package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.UserService;
import dev.thural.quietspacebackend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
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
    ArgumentCaptor<UUID> objectIdArgumentCaptor;

    @Captor
    ArgumentCaptor<UserDTO> userArgumentCaptor;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @MockBean
    UserService userService;

    UserService userServiceImpl;

    @BeforeEach
    void setUp() {
        userServiceImpl = new UserServiceImpl(userRepository);
    }

    @Test
    void getAllUsers() throws Exception {
        List<UserDTO> testUsers = userServiceImpl.getAll();

        given(userService.getAll()).willReturn(testUsers);

        mockMvc.perform(get(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testUsers.size())));
    }

    @Test
    void getUserById() throws Exception {
        UserDTO testUser = userServiceImpl.getAll().get(0);

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
        List<UserDTO> testUsers = userServiceImpl.getAll();
        UserDTO testUser = testUsers.get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        given(userService.addOne(any(UserDTO.class))).willReturn(testUsers.get(1));

        mockMvc.perform(post(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void updateUser() throws Exception {
        List<UserDTO> testUsers = userServiceImpl.getAll();

        UserDTO testUser = testUsers.get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        mockMvc.perform(put(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).updateOne(any(UUID.class), any(UserDTO.class));
    }

    @Test
    void deleteUser() throws Exception {
        List<UserDTO> testUsers = userServiceImpl.getAll();
        UserDTO testUser = testUsers.get(0);

        mockMvc.perform(delete(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteOne(objectIdArgumentCaptor.capture());

        assertThat(testUser.getId()).isEqualTo(objectIdArgumentCaptor.getValue());
    }

    @Test
    void patchUser() throws Exception {
        List<UserDTO> testUsers = userServiceImpl.getAll();

        UserDTO testUser = testUsers.get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        mockMvc.perform(patch(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).patchOne(objectIdArgumentCaptor.capture(), userArgumentCaptor.capture());

        assertThat(testUser.getId()).isEqualTo(objectIdArgumentCaptor.getValue());
        assertThat(testUser.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(testUser.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
    }

    @Test
    void userByIdNotFound() throws Exception {
        given(userService.getById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(UserController.USER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

}