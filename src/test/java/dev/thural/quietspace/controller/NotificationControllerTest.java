package dev.thural.quietspace.controller;

import dev.thural.quietspace.enums.EntityType;
import dev.thural.quietspace.enums.NotificationType;
import dev.thural.quietspace.model.response.NotificationResponse;
import dev.thural.quietspace.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

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
        when(notificationService.getAllNotifications(any(), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("FOLLOW_REQUEST"));
    }

    @Test
    void getNotificationsByType_shouldReturnFilteredPage() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .type(NotificationType.FOLLOW_REQUEST)
                .build();
        when(notificationService.getNotificationsByType(any(), any(), eq("FOLLOW_REQUEST")))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/notifications/type/FOLLOW_REQUEST")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
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