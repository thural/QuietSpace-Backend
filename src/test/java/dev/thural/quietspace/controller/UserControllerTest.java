package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.service.CommentService;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    ArgumentCaptor<UserRequest> userArgumentCaptor;

    @MockBean
    UserService userService;
    @MockBean
    TokenRepository tokenRepository;
    @MockBean
    PostService postService;
    @MockBean
    CommentService commentService;

    @InjectMocks
    UserController userController;

    UUID userId;
    UserRequest testUserRequest;
    UserResponse testUserResponse;



    @BeforeEach
    void setUp() {
        this.userId = UUID.randomUUID();

        this.testUserRequest = UserRequest.builder()
                .username("user")
                .role("user")
                .email("user@email.com")
                .build();
    }

    @Test
    void getUserById() throws Exception {
        given(userService.getUserById(any())).willReturn(Optional.of(testUserResponse));

        mockMvc.perform(get(UserController.USER_PATH + "/" + testUserResponse.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUserResponse.getId().toString())))
                .andExpect(jsonPath("$.username", is(testUserResponse.getUsername())));
    }

    @Test
    void patchUser() throws Exception {
        testUserRequest.setUsername("testUserUpdated");
        testUserRequest.setPassword("testPasswordUpdated");

        mockMvc.perform(patch(UserController.USER_PATH, testUserRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isNoContent());

        verify(userService).patchUser(userArgumentCaptor.capture());

        assertThat(testUserRequest.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(testUserRequest.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
    }

    @Test
    void userByIdNotFound() throws Exception {
        given(userService.getUserById(userId)).willReturn(Optional.empty());

        mockMvc.perform(get(UserController.USER_PATH + "/" + userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).getUserById(uuidArgumentCaptor.capture());
    }

}