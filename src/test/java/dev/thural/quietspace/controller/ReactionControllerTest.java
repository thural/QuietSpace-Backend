package dev.thural.quietspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.enums.EntityType;
import dev.thural.quietspace.enums.ReactionType;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ReactionControllerTest {

    MockMvc mockMvc;

    @Mock
    private ReactionService reactionService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReactionController reactionController;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reactionController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getReactionsByUser_shouldReturn200WithPage() throws Exception {
        when(reactionService.getReactionsByUserIdAndContentType(any(UUID.class), any(EntityType.class), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/reactions/user")
                        .param("userId", UUID.randomUUID().toString())
                        .param("contentType", "POST")
                        .param("page-number", "0")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getReactionsByContent_shouldReturn200WithPage() throws Exception {
        when(reactionService.getReactionsByContentIdAndContentType(any(UUID.class), any(EntityType.class), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/reactions/content")
                        .param("contentId", UUID.randomUUID().toString())
                        .param("contentType", "POST")
                        .param("page-number", "0")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void toggleReaction_shouldCallServiceAndNotificationThenReturn200() throws Exception {
        ReactionRequest request = ReactionRequest.builder()
                .contentId(UUID.randomUUID())
                .contentType(EntityType.POST)
                .reactionType(ReactionType.LIKE)
                .build();

        mockMvc.perform(post("/api/v1/reactions/toggle-reaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void countByContentIdAndReactionType_shouldReturn200WithCount() throws Exception {
        when(reactionService.countByContentIdAndReactionType(any(UUID.class), any(ReactionType.class)))
                .thenReturn(3);

        mockMvc.perform(get("/api/v1/reactions/count")
                        .param("contentId", UUID.randomUUID().toString())
                        .param("type", "LIKE"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
