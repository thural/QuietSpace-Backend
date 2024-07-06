package dev.thural.quietspace.controller;

import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @MockBean
    PostService postService;
    @MockBean
    ReactionService reactionService;
    @MockBean
    JwtService jwtService;
    @MockBean
    TokenRepository tokenRepository;
    @MockBean
    RoleRepository roleRepository;

    @Test
    void getAllPosts() {
    }

    @Test
    void getPostsByQuery() {
    }

    @Test
    void createPost() {
    }

    @Test
    void getPostById() throws Exception {
        mockMvc.perform(get(PostController.POST_PATH + "/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void putPost() {
    }

    @Test
    void deletePost() {
    }

    @Test
    void patchPost() {
    }

    @Test
    void getAllLikesByPostId() {
    }

    @Test
    void togglePostLike() {
    }

    @Test
    void votePoll() {
    }
}