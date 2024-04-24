package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.model.response.UserResponse;
import dev.thural.quietspacebackend.repository.TokenRepository;
import dev.thural.quietspacebackend.service.CommentService;
import dev.thural.quietspacebackend.service.PostService;
import dev.thural.quietspacebackend.service.UserService;
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
    ArgumentCaptor<UserResponse> userArgumentCaptor;

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
    UserResponse testUser;



    @BeforeEach
    void setUp() {
        this.userId = UUID.randomUUID();

        this.testUser = UserResponse.builder()
                .id(userId)
                .username("user")
                .role("user")
                .email("user@email.com")
                .password("pAssWoRd")
                .build();
    }

    @Test
    void getUserById() throws Exception {
        given(userService.getUserById(any())).willReturn(Optional.of(testUser));

        mockMvc.perform(get(UserController.USER_PATH + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
    }

    @Test
    void patchUser() throws Exception {
        testUser.setUsername("testUserUpdated");
        testUser.setPassword("testPasswordUpdated");

        mockMvc.perform(patch(UserController.USER_PATH, testUser)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNoContent());

        verify(userService).patchUser(userArgumentCaptor.capture());

        assertThat(testUser.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(testUser.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
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