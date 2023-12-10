package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.UserService;
import dev.thural.quietspacebackend.service.UserServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

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
        List<User> testUsers = userServiceImpl.getAll();

        given(userService.getAll()).willReturn(testUsers);

        mockMvc.perform(get("api/v1/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testUsers.size())));
    }

    @Test
    void getUserById() throws Exception {

        User testUser = userServiceImpl.getAll().get(0);

        given(userService.getById(testUser.getId()))
                .willReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/users" + "/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
    }

    @Test
    void createUser() throws Exception {

        List<User> testUsers = userServiceImpl.getAll();

        User testUser = testUsers.get(0);
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");

        given(userService.addOne(any(User.class))).willReturn(testUsers.get(1));

        mockMvc.perform(post("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }
}
