package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerIT {

    @Autowired
    UserController userController;
    @Autowired
    UserRepository userRepository;
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

    @WithUserDetails("admin@email.com")
    @Test
    void testUpdateUserNameTooLong() throws Exception {
        User randomUser = userRepository.findFirstByOrderByUsernameDesc().orElseThrow();
        User user = userRepository.findUserEntityByEmail(randomUser.getEmail()).orElseThrow();
        UserResponse userResponse = userMapper.toResponse(user);
        userResponse.setUsername("a long user name to cause transaction exception");
        mockMvc.perform(patch(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userResponse)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void testListSearchUserByName() throws Exception {
        User randomUser = userRepository.findFirstByOrderByUsernameDesc().orElseThrow();
        mockMvc.perform(get(UserController.USER_PATH + "/search")
                        .queryParam("username", randomUser.getUsername())
                        .queryParam("page-number", "1")
                        .queryParam("page-size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(11)))
                .andExpect(jsonPath("$.content.[0].username", is(randomUser.getUsername())));
    }

    @Test
    void testListUsers() throws Exception {
        User randomUser = userRepository.findFirstByOrderByUsernameDesc().orElseThrow();
        mockMvc.perform(get(UserController.USER_PATH + "/query")
                        .queryParam("username", randomUser.getUsername())
                        .queryParam("firstname", randomUser.getFirstname())
                        .queryParam("lastname", randomUser.getLastname())
                        .queryParam("page-number", "1")
                        .queryParam("page-size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(1)))
                .andExpect(jsonPath("$.size()", is(11)));
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteAllUsers() {
        userRepository.deleteAll();
        Page<UserResponse> userList = userController.listUsersBySearchTerm("", 1, 25);
        assertThat(userList.getContent().size()).isEqualTo(0);
    }

    @Test
    void testGetById() {
        User user = userRepository.findAll().get(0);
        ResponseEntity<?> response = userController.getUserById(user.getId());
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testUserNotFound() {
        ResponseEntity<UserResponse> response = userController.getUserById(UUID.randomUUID());
        assertThat(response).isEqualTo(ResponseEntity.notFound().build());
    }

    @WithUserDetails("admin@email.com")
    @Rollback
    @Transactional
    @Test
    void testUpdateExistingUser() {
        User randomUser = userRepository.findFirstByOrderByUsernameDesc().orElseThrow();
        User user = userRepository.findUserEntityByEmail(randomUser.getEmail()).orElseThrow();
        UserRegisterRequest userRegisterRequest = userMapper.toRequest(user);
        final String updatedName = "updatedName";
        userRegisterRequest.setUsername(updatedName);

        ResponseEntity<?> response = userController.patchUser(userRegisterRequest);

        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo(updatedName);
    }

    @WithUserDetails("admin@email.com")
    @Rollback
    @Transactional
    @Test
    void testDeleteUser() {
        User user = userRepository.findAll().get(0);
        ResponseEntity<?> response = userController.deleteUser(user.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

}