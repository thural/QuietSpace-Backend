package dev.thural.quietspace.controller.slice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.controller.UserController;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

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
    @MockBean
    ReactionService reactionService;
    @MockBean
    JwtService jwtService;
    @MockBean
    RoleRepository roleRepository;
    @MockBean
    FollowService followService;

    private UUID userId;
    private UserRegisterRequest registerRequest;
    private UserResponse userResponse;


    @BeforeEach
    void setUp() {

        this.userId = UUID.randomUUID();

        this.registerRequest = UserRegisterRequest.builder()
                .username("user")
                .role("user")
                .email("user@email.com")
                .build();

        this.userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("user")
                .email("user@email.com")
                .role("USER")
                .build();
    }

    @Test
    @WithAnonymousUser
    void listUsersPaginated() throws Exception {

        mockMvc.perform(get(UserController.USER_PATH)
                        .param("page-number", "0")
                        .param("page-size", "10")
                        .param("username", "user"))
                .andExpect(status().isOk());

        verify(userService, times(1)).listUsers("user", 0, 10);
    }

    @Test
    void listUsersPaginatedQuery() throws Exception {

        mockMvc.perform(get(UserController.USER_PATH + "/search")
                        .param("query", "user")
                        .param("page-number", "0")
                        .param("page-size", "10"))
                .andExpect(status().isOk());

        verify(userService).listUsersByQuery("user", 0, 10);
    }


    @Test
    void getUserById() throws Exception {
        when(userService.getUserResponseById(userId)).thenReturn(Optional.of(userResponse));

        mockMvc.perform(get(UserController.USER_PATH + "/" + userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userResponse.getId().toString())))
                .andExpect(jsonPath("$.username", is(userResponse.getUsername())));

        verify(userService, times(1)).getUserResponseById(uuidArgumentCaptor.capture());
        assertThat(userId).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void deleteUserById() throws Exception {

        mockMvc.perform(delete(UserController.USER_PATH + "/" + userId))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).deleteUserById(uuidArgumentCaptor.capture());
        assertThat(userId).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void patchUser() throws Exception {
        registerRequest.setUsername("testUserUpdated");
        registerRequest.setPassword("testPasswordUpdated");

        String userBodyJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(patch(UserController.USER_PATH, registerRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userBodyJson))
                .andExpect(status().isOk());

        verify(userService).patchUser(userArgumentCaptor.capture());

        assertThat(registerRequest.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
        assertThat(registerRequest.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
    }

    @Test
    void userByIdNotFound() throws Exception {

        mockMvc.perform(get(UserController.USER_PATH + "/" + userId))
                .andExpect(status().isNotFound());

        verify(userService).getUserResponseById(uuidArgumentCaptor.capture());
        assertThat(userId).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void getAuthenticatedUser() throws Exception {
        when(userService.getLoggedUserResponse()).thenReturn(Optional.of(userResponse));

        mockMvc.perform(get(UserController.USER_PATH + "/" + "profile")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is(userResponse.getUsername())))
                .andExpect(status().isOk());

        verify(userService, times(1)).getLoggedUserResponse();
    }


}