package dev.thural.quietspace.photo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.photo.PhotoController;
import dev.thural.quietspace.photo.dto.PhotoResponse;
import dev.thural.quietspace.security.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.photo.PhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = PhotoController.class)
class PhotoControllerSliceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PhotoService photoService;
    @MockitoBean
    TokenRepository tokenRepository;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserDetailsService userDetailsService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }

    @Test
    void uploadProfilePhoto_shouldReturn201() throws Exception {
        when(photoService.uploadProfilePhoto(any())).thenReturn("photo-name.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "photo-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/photos/profile")
                        .file(file))
                .andExpect(status().isCreated());
    }

    @Test
    void getPhotoByName_shouldReturn200() throws Exception {
        PhotoResponse photoResponse = PhotoResponse.builder()
                .name("test.jpg")
                .type("image/jpeg")
                .data(new byte[]{1, 2, 3})
                .build();
        when(photoService.getPhotoByName("test.jpg")).thenReturn(photoResponse);

        mockMvc.perform(get("/api/v1/photos/{name}", "test.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    void removePhotoByUserId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/photos/profile/{userId}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}
