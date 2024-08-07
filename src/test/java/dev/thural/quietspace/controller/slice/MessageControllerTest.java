package dev.thural.quietspace.controller.slice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.controller.MessageController;
import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MessageController.class)
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;
    @MockBean
    private ReactionService reactionService;
    @MockBean
    PostService postService;
    @MockBean
    JwtService jwtService;
    @MockBean
    TokenRepository tokenRepository;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<MessageRequest> messageRequestArgumentCaptor;

    private MessageRequest messageRequest;
    private MessageResponse messageResponse;


    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .email("user@email.com")
                .password("pAsSword")
                .build();

        Chat chat = Chat.builder()
                .id(UUID.randomUUID())
                .build();

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .text("sample text")
                .chat(chat)
                .sender(user)
                .build();
        this.messageResponse = MessageResponse.builder()
                .id(UUID.randomUUID())
                .text("sample text")
                .chatId(chat.getId())
                .senderId(user.getId())
                .senderName(user.getUsername())
                .build();

        this.messageRequest = MessageRequest.builder()
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .text(message.getText())
                .build();
    }

    @Test
    void createMessage() throws Exception {
        when(messageService.addMessage(any())).thenReturn(messageResponse);

        mockMvc.perform(post(MessageController.MESSAGE_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(jsonPath("$.username", is(messageResponse.getSenderName())))
                .andExpect(jsonPath("$.text", is(messageResponse.getText())))
                .andExpect(jsonPath("$.id", is(messageResponse.getId().toString())))
                .andExpect(jsonPath("$.senderId", is(messageResponse.getSenderId().toString())))
                .andExpect(jsonPath("$.chatId", is(messageResponse.getChatId().toString())))
                .andExpect(status().isOk());

        verify(messageService).addMessage(messageRequestArgumentCaptor.capture());
        assertThat(messageResponse.getText()).isEqualTo(messageRequestArgumentCaptor.getValue().getText());
    }

    @Test
    void deleteMessage() throws Exception {
        mockMvc.perform(delete(MessageController.MESSAGE_PATH + "/" + messageResponse.getId()))
                .andExpect(status().isNoContent());

        verify(messageService).deleteMessage(uuidArgumentCaptor.capture());
        assertThat(messageResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void getMessagesByChatId() throws Exception {
        when(messageService.getMessagesByChatId(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get(MessageController.MESSAGE_PATH + "/chat/" + messageResponse.getId())
                        .param("page-number", "1")
                        .param("page-size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(messageService).getMessagesByChatId(1, 10, messageResponse.getId());
    }

}