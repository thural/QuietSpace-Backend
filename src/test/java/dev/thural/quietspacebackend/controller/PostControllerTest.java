package dev.thural.quietspacebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.model.Post;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.service.PostService;
import dev.thural.quietspacebackend.service.PostServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PostController.class)
public class PostControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;

    @Autowired
    ObjectMapper objectMapper;

    PostService postServiceImpl;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postServiceImpl = new PostServiceImpl(postRepository);
    }

    @Test
    void getAllPosts() throws Exception {
        List<Post> testPosts = postServiceImpl.getAll();

        given(postService.getAll()).willReturn(testPosts);

        mockMvc.perform(get("/api/v1/posts/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testPosts.size())));
    }

    @Test
    void getPostById() throws Exception {

        Post testPost = postServiceImpl.getAll().get(0);

        given(postService.getById(testPost.getId()))
                .willReturn(Optional.of(testPost));

        mockMvc.perform(get("/api/v1/posts" + "/" + testPost.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testPost.getId().toString())))
                .andExpect(jsonPath("$.text", is(testPost.getText())));
    }

    @Test
    void createPost() throws Exception {
        List<Post> testPosts = postServiceImpl.getAll();
        Post testPost = testPosts.get(0);
        testPost.setText("testText");

        given(postService.addOne(any(Post.class)))
                .willReturn(testPosts.get(1));

        mockMvc.perform(post("/api/v1/posts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPost)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void updatePost() throws Exception {
        List<Post> testPosts = postServiceImpl.getAll();

        Post testPost = testPosts.get(0);
        testPost.setText("testText");

        mockMvc.perform(put("/api/v1/posts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPost)))
                .andExpect(status().isNoContent());

        verify(postService).updateOne(any(ObjectId.class), any(Post.class));
    }
}