package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.config.AppConfig;
import dev.thural.quietspacebackend.config.JwtValidator;
import dev.thural.quietspacebackend.config.SecurityConfig;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.service.AuthService;
import dev.thural.quietspacebackend.service.PostService;
import dev.thural.quietspacebackend.service.UserService;
import dev.thural.quietspacebackend.service.impls.UserServiceImpl;
import dev.thural.quietspacebackend.utils.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
public class UserControllerTest {

//
//    @MockBean
//    AppConfig appConfig;
//    @MockBean
//    SecurityConfig securityConfig;
//    @MockBean
//    JwtProvider jwtProvider;
//
//    @Captor
//    ArgumentCaptor<UUID> uuidArgumentCaptor;
//
//    @Captor
//    ArgumentCaptor<UserDTO> userArgumentCaptor;
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @MockBean
//    UserRepository userRepository;

//    @Autowired
//    UserRepository userRepository;
//    UserService userServiceImpl;
//    UserDetailsService userDetailsService;
//    PasswordEncoder passwordEncoder;
//    UserMapper userMapper;

//    @BeforeEach
//    void setUp() {
//        userServiceImpl = new UserServiceImpl(passwordEncoder, userDetailsService, userMapper, userRepository );
//    }

//    @Autowired
//    private WebApplicationContext context;

//    @MockBean
//    UserController userController;

//    @Autowired
//    MockMvc mockMvc;
//
//
//
//    UserServiceImpl userServiceImpl;
//
//
//    @Test
//    void getUserById() throws Exception {
//        UserEntity user = userRepository
//                .findById(UUID.fromString("e18d0c0c-37a4-4e50-8041-bd49ffde8182"))
//                .orElseThrow(EntityNotFoundException::new);
//
//        System.out.println("found user form test repository: " + user.getEmail());

//
//
//        UserDTO testUser = userServiceImpl.listUsersByQuery("t", 10, 15)
//                .getContent().get(0);
//
//        System.out.println("found test user email: " + testUser.getEmail());
//
//        given(userService.getUserById(testUser.getId()))
//                .willReturn(Optional.of(testUser));
//
//        mockMvc.perform(get(UserController.USER_PATH, UUID.randomUUID())
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
//                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
//    }

//    @Test
//    void getAllUsers() throws Exception {
//        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);
//
//        given(userService.listUsers(any(), any(), any())).willReturn(testUsers);
//
//        mockMvc.perform(get(UserController.USER_PATH)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.length()", is(testUsers.getContent().size())));
//    }
//
//    @Test
//    void createUser() throws Exception {
//        AuthResponse authResponse = new AuthResponse("tokenTokenToken", "user created", "7817398717");
//
//        given(userService.registerUser(any(UserDTO.class))).willReturn(authResponse);
//
//        mockMvc.perform(post(UserController.USER_PATH)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(authResponse)))
//                .andExpect(status().isCreated())
//                .andExpect(header().exists("Location"));
//    }
//
//    @Test
//    void updateUser() throws Exception {
//        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);
//
//        UserDTO testUser = testUsers.getContent().get(0);
//        testUser.setUsername("testUser");
//        testUser.setPassword("testPassword");
//
//        given(userService.updateUser(any(), any())).willReturn(Optional.of(testUser));
//
//        mockMvc.perform(put(UserController.USER_PATH + "/" + testUser.getId())
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testUser)))
//                .andExpect(status().isNoContent());
//
//        verify(userService).updateUser(any(UUID.class), any(UserDTO.class));
//    }
//
//    @Test
//    void deleteUser() throws Exception {
//        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);
//        UserDTO testUser = testUsers.getContent().get(0);
//
//        given(userService.deleteUser(any(), )).willReturn(true);
//
//        mockMvc.perform(delete(UserController.USER_PATH + "/" + testUser.getId())
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNoContent());
//
//        verify(userService).deleteUser(uuidArgumentCaptor.capture(), );
//
//        assertThat(testUser.getId()).isEqualTo(uuidArgumentCaptor.getValue());
//    }
//
//    @Test
//    void patchUser() throws Exception {
//        Page<UserDTO> testUsers = userServiceImpl.listUsers(null, null, null);
//
//        UserDTO testUser = testUsers.getContent().get(0);
//        testUser.setUsername("testUser");
//        testUser.setPassword("testPassword");
//
//        mockMvc.perform(patch(UserController.USER_PATH + "/" + testUser.getId())
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testUser)))
//                .andExpect(status().isNoContent());
//
//        verify(userService).patchOne(uuidArgumentCaptor.capture(), userArgumentCaptor.capture(), );
//
//        assertThat(testUser.getId()).isEqualTo(uuidArgumentCaptor.getValue());
//        assertThat(testUser.getUsername()).isEqualTo(userArgumentCaptor.getValue().getUsername());
//        assertThat(testUser.getPassword()).isEqualTo(userArgumentCaptor.getValue().getPassword());
//    }
//
//    @Test
//    void userByIdNotFound() throws Exception {
//        given(userService.getUserById(any(UUID.class))).willReturn(Optional.empty());
//
//        mockMvc.perform(get(UserController.USER_PATH_ID, UUID.randomUUID()))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void createUserNullUserName() throws Exception {
//        UserDTO userDTO = UserDTO.builder().build();
//
//        given(userService.registerUser(any(UserDTO.class)))
//                .willReturn(userService.listUsers(null, null, null)
//                .getContent().get(0));
//
//        MvcResult result = mockMvc.perform(post(UserController.USER_PATH)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userDTO)))
//                .andExpect(status().isBadRequest()).andReturn();
//
//        System.out.println(result.getResponse().getContentAsString());
//    }

}