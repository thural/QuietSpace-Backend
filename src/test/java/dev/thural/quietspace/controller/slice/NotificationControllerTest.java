package dev.thural.quietspace.controller.slice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.controller.NotificationController;
import dev.thural.quietspace.enums.NotificationType;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NotificationService notificationService;
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
    void getAllNotifications_shouldReturnPage() throws Exception {
        when(notificationService.getAllNotifications(anyInt(), anyInt())).thenReturn(Page.empty());

        mockMvc.perform(get(NotificationController.NOTIFICATION_PATH)
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getNotificationsByType_shouldReturnPage() throws Exception {
        when(notificationService.getNotificationsByType(anyInt(), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get(NotificationController.NOTIFICATION_PATH + "/type/{type}", "POST_REACTION")
                        .param("page-number", "1")
                        .param("page-size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getCountOfPendingNotifications_shouldReturnCount() throws Exception {
        when(notificationService.getCountOfPendingNotifications()).thenReturn(5);

        mockMvc.perform(get(NotificationController.NOTIFICATION_PATH + "/count-pending"))
                .andExpect(status().isOk());
    }

    @Test
    void handleSeen_shouldReturn202() throws Exception {
        mockMvc.perform(post(NotificationController.NOTIFICATION_PATH + "/seen/{contentId}", UUID.randomUUID()))
                .andExpect(status().isAccepted());
    }
}
