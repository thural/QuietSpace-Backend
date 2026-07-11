package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.config.TestcontainersConfig;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.PhotoService;
import dev.thural.quietspace.utils.IntegrationTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class PostFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private PhotoService photoService;

    private IntegrationTestHelper helper;
    private String jwtToken;
    private UUID userId;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        helper = new IntegrationTestHelper(mockMvc, objectMapper, userRepository, passwordEncoder);
        jwtToken = helper.registerAndLogin("postuser@test.com", "password123");
        userId = userRepository.findUserEntityByEmail("postuser@test.com").orElseThrow().getId();
    }

    @Test
    void createPost_givenValidRequest_shouldReturn200() throws Exception {
        PostRequest request = PostRequest.builder()
                .userId(userId)
                .title("Test Post")
                .text("This is a test post content")
                .build();

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "post", "", "application/json",
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("This is a test post content"))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    void createPost_givenEmptyText_shouldReturn400() throws Exception {
        PostRequest request = PostRequest.builder()
                .userId(userId)
                .text("")
                .build();

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "post", "", "application/json",
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPostById_givenExistingId_shouldReturn200() throws Exception {
        PostRequest request = PostRequest.builder()
                .userId(userId)
                .title("Test Post")
                .text("This is a test post content")
                .build();

        String responseBody = mockMvc.perform(multipart("/api/v1/posts")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "post", "", "application/json",
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String postId = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId));
    }

    @Test
    void getPostById_givenNonExistentId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{postId}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePost_givenOwnPost_shouldReturn200() throws Exception {
        PostRequest request = PostRequest.builder()
                .userId(userId)
                .title("Original Title")
                .text("Original text")
                .build();

        String responseBody = mockMvc.perform(multipart("/api/v1/posts")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "post", "", "application/json",
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String postId = objectMapper.readTree(responseBody).get("id").asText();

        PostRequest update = PostRequest.builder()
                .userId(userId)
                .title("Updated Title")
                .text("Updated text")
                .build();

        mockMvc.perform(put("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    void deletePost_givenOwnPost_shouldReturn204() throws Exception {
        PostRequest request = PostRequest.builder()
                .userId(userId)
                .title("Test Post")
                .text("This is a test post content")
                .build();

        String responseBody = mockMvc.perform(multipart("/api/v1/posts")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "post", "", "application/json",
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String postId = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(delete("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllPosts_shouldReturnPagedResults() throws Exception {
        PostRequest request = PostRequest.builder()
                .userId(userId)
                .title("Test Post")
                .text("This is a test post content")
                .build();

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "post", "", "application/json",
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/posts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page-number", "1")
                        .param("page-size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
