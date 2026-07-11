package dev.thural.quietspace.controller;

import dev.thural.quietspace.enums.EntityType;
import dev.thural.quietspace.enums.NotificationType;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    void handleSeen_shouldReturn202() throws Exception {
        UUID contentId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/notifications/seen/{contentId}", contentId))
                .andExpect(status().isAccepted());
    }

    @Test
    void getAllNotifications_shouldReturnPage() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .type(NotificationType.FOLLOW_REQUEST)
                .build();
        when(notificationService.getAllNotifications(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page-number", "0")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("FOLLOW_REQUEST"));
    }

    @Test
    void getNotificationsByType_shouldReturnFilteredPage() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .type(NotificationType.FOLLOW_REQUEST)
                .build();
        when(notificationService.getNotificationsByType(anyInt(), anyInt(), eq("FOLLOW_REQUEST")))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/notifications/type/FOLLOW_REQUEST")
                        .param("page-number", "0")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getCountOfPendingNotifications_shouldReturn200WithCount() throws Exception {
        when(notificationService.getCountOfPendingNotifications()).thenReturn(5);

        mockMvc.perform(get("/api/v1/notifications/count-pending"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void processNotificationByReaction_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/process-reaction")
                        .param("type", "POST")
                        .param("contentId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }
}
