package dev.thural.quietspace.controller.slice;

import dev.thural.quietspace.controller.PostController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPostById_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllPosts_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/posts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletePost_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}