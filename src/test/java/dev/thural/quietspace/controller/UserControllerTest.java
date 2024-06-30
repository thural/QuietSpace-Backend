package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.model.request.UserRegisterRequest;
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
    ArgumentCaptor<UserRegisterRequest> userArgumentCaptor;

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
    UserRegisterRequest testUserRegisterRequest;
    UserResponse testUserResponse;


    @BeforeEach
    void setUp() {
        this.userId = UUID.randomUUID();

        this.testUserRegisterRequest = UserRegisterRequest.builder()
                .username("user")
                .role("user")
                .email("user@email.com")
                .build();
    }

    @Test
    void getUserById() throws Exception {
        given(userService.getUserResponseById(any())).willReturn(Optional.of(testUserResponse));

        mockMvc.perform(get(UserController.USER_PATH + "/" + testUserResponse.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUserResponse.getId().toString())))
                .andExpect(jsonPath("$.username", is(testUserResponse.getUsername())));
    }

    @Test
    void patchUser() throws Exception {
        testUserRegisterRequest.setUsername("testUserUpdated");
        testUserRegisterRequest.setPassword("testPasswordUpdated");

        mockMvc.perform(patch(UserController.USER_PATH, testUserRegisterRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRegisterRequest)))
                .andExpect(status().isNoContent());

        verify(userService).patchUser(userArgumentCaptor.capture());

        assertThat(testUserRegisterRequest.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(testUserRegisterRequest.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
    }

    @Test
    void userByIdNotFound() throws Exception {
        given(userService.getUserResponseById(userId)).willReturn(Optional.empty());

        mockMvc.perform(get(UserController.USER_PATH + "/" + userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).getUserResponseById(uuidArgumentCaptor.capture());
    }

}