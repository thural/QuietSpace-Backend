package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.config.TestcontainersConfig;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.utils.IntegrationTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import jakarta.persistence.EntityManager;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class PhotoFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private IntegrationTestHelper helper;
    private String jwtToken;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        IntegrationTestHelper.cleanDatabase(entityManager);
        userRepository.deleteAll();
        helper = new IntegrationTestHelper(mockMvc, objectMapper, userRepository, passwordEncoder);
        jwtToken = helper.registerAndLogin("photouser@test.com", "password123");
        userId = userRepository.findUserEntityByEmail("photouser@test.com").orElseThrow().getId();
    }

    @Test
    void uploadProfilePhoto_shouldReturn201() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", MediaType.IMAGE_JPEG_VALUE,
                new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0 }
        );

        mockMvc.perform(multipart("/api/v1/photos/profile")
                        .file(image)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadProfilePhoto_givenUnsupportedType_shouldReturn400() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "file.txt", MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/profile")
                        .file(image)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPhotoByName_givenNonExistentName_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/photos/{name}", "nonexistent.jpg")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProfilePhoto_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/photos/profile/{userId}", userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
