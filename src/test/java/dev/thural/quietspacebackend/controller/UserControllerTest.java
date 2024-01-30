package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.config.JwtValidator;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDto;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.service.AuthService;
import dev.thural.quietspacebackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<UserDto> userArgumentCaptor;
    @Captor
    ArgumentCaptor<String> authHeaderCaptor;

    @Mock
    UserService userServiceImpl;
    @Mock
    AuthService authService;
    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    @Test
    void getUserById() throws Exception {
        UUID userId = UUID.randomUUID();

        UserDto testUser = UserDto.builder()
                .id(userId)
                .username("user")
                .role("user")
                .email("user@email.com")
                .password("pAssWoRd")
                .build();

        given(userService.getUserById(testUser.getId())).willReturn(Optional.of(testUser));

        mockMvc.perform(get(UserController.USER_PATH, userId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
    }


    @Test
    void getAllUsers() throws Exception {
        Page<UserDto> testUsers = userServiceImpl.listUsers("user", 0, 25);

        given(userService.listUsers(any(), any(), any())).willReturn(testUsers);

        mockMvc.perform(get(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testUsers.getContent().size())));
    }


    @Test
    void createUser() throws Exception {
        AuthResponse authResponse = new AuthResponse("tokenTokenToken", "user created", "7817398717");

        given(authService.register(any(UserDto.class))).willReturn(authResponse);

        mockMvc.perform(post(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authResponse)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }


    @Test
    void updateUser() throws Exception {
        Page<UserDto> testUsers = userServiceImpl.listUsers(null, 0, 25);

        UserDto testUser = testUsers.getContent().get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        given(userService.updateUser(any(String.class), any(UserDto.class))).willReturn(Optional.of(testUser));

        mockMvc.perform(put(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).updateUser(any(String.class), any(UserDto.class));
    }


    @Test
    void deleteUser() throws Exception {
        Page<UserDto> testUsers = userServiceImpl.listUsers(null, 0, 25);
        UserDto testUser = testUsers.getContent().get(0);

        given(userService.deleteUser(any(UUID.class), any(String.class))).willReturn(true);

        mockMvc.perform(delete(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(uuidArgumentCaptor.capture(), authHeaderCaptor.capture());

        assertThat(testUser.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }


    @Test
    void patchUser() throws Exception {
        Page<UserDto> testUsers = userServiceImpl.listUsers(null, 0, 25);

        UserDto testUser = testUsers.getContent().get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        mockMvc.perform(patch(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).patchUser(userArgumentCaptor.capture(), authHeaderCaptor.capture());

        assertThat(testUser.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(testUser.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(testUser.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
    }


    @Test
    void userByIdNotFound() throws Exception {
        given(userService.getUserById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(UserController.USER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }


    @Test
    void createUserNullEmail() throws Exception {
        UUID userId = UUID.randomUUID();

        UserDto userDTO = UserDto.builder()
                .id(userId)
                .password("pAssWord")
                .build();

        given(authService.register(any(UserDto.class))).willReturn(any(AuthResponse.class));

        mockMvc.perform(post(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest()).andReturn();
    }

}