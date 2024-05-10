package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void testListUsers() throws Exception {
        mockMvc.perform(get(UserController.USER_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(4)))
                .andExpect(jsonPath("$.size()", is(11)));
    }

    @WithUserDetails("tural@email.com")
    @Test
    void testUpdateUserNameTooLong() throws Exception {

        User user = userRepository.findUserEntityByEmail("tural@email.com")
                .orElseThrow();

        UserResponse userResponse = userMapper.userEntityToResponse(user);

        userResponse.setUsername("a long user name to cause transaction exception");

        MvcResult result = mockMvc.perform(patch(UserController.USER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userResponse)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(3))).andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void testListUserByNamePage1() throws Exception {
        mockMvc.perform(get(UserController.USER_PATH)
                        .queryParam("username", "john")
                        .queryParam("page-number", "1")
                        .queryParam("page-size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(11)))
                .andExpect(jsonPath("$.content.[0].username", is("john")));
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteAllUsers() {
        userRepository.deleteAll();
        Page<UserResponse> userList = userController.listUsers(null, 1, 25);
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
        assertThrows(UserNotFoundException.class, () -> userController.getUserById(UUID.randomUUID()));
    }

    @WithUserDetails("tural@email.com")
    @Rollback
    @Transactional
    @Test
    void testUpdateExistingUser() {
        User user = userRepository.findUserEntityByEmail("tural@email.com").orElseThrow();
        UserRequest userRequest = userMapper.userEntityToRequest(user);
        final String updatedName = "updatedName";
        userRequest.setUsername(updatedName);

        ResponseEntity<?> response = userController.patchUser(userRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo(updatedName);
    }

    @WithUserDetails("tural@email.com")
    @Test
    void testUpdateNotFound() {
        assertThrows(UserNotFoundException.class, () -> userController.patchUser(UserRequest.builder().build()));
    }

    @WithUserDetails("tural@email.com")
    @Rollback
    @Transactional
    @Test
    void testDeleteUser() {
        User user = userRepository.findAll().get(0);

        ResponseEntity<?> response = userController.deleteUser("auth header", user.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

}