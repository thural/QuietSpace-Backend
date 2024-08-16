package dev.thural.quietspace.controller.slice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thural.quietspace.controller.ChatController;
import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.ChatService;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ChatController.class)
class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ChatService chatService;
    @MockBean
    MessageService messageService;
    @MockBean
    ReactionService reactionService;
    @MockBean
    TokenRepository tokenRepository;
    @MockBean
    JwtService jwtService;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<ChatRequest> chatRequestArgumentCaptor;

    private User user1;
    private User user2;
    private Chat chat;
    private ChatRequest chatRequest;
    private ChatResponse chatResponse;
    private MessageRequest messageRequest;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        this.user1 = User.builder()
                .id(UUID.randomUUID())
                .build();
        this.user2 = User.builder()
                .id(UUID.randomUUID())
                .build();

        UserResponse userResponse1 = UserResponse.builder()
                .id(user1.getId())
                .build();
        UserResponse userResponse2 = UserResponse.builder()
                .id(user2.getId())
                .build();

        this.chat = Chat.builder()
                .id(UUID.randomUUID())
                .users(List.of(user1, user2))
                .messages(List.of())
                .build();

        this.chatRequest = ChatRequest.builder()
                .userIds(List.of(user1.getId(), user2.getId()))
                .build();

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .sender(user1)
                .chat(chat)
                .text("sample text")
                .build();

        this.messageResponse = MessageResponse.builder()
                .id(message.getId())
                .chatId(chat.getId())
                .text(message.getText())
                .build();

        this.chatResponse = ChatResponse.builder()
                .id(chat.getId())
                .members(List.of(userResponse1, userResponse2))
                .recentMessage(messageResponse)
                .build();
    }

    @Test
    void getSingleChatById() throws Exception {
        when(chatService.getChatById(any())).thenReturn(chatResponse);

        mockMvc.perform(get(ChatController.SOCKET_CHAT_PATH + "/" + chat.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(chat.getId().toString())))
                .andExpect(jsonPath("$.recentMessage.text", is(messageResponse.getText())))
                .andExpect(jsonPath("$.members.size()", is(chatResponse.getMembers().size())))
                .andExpect(jsonPath("$.members[0].id", is(chatResponse.getMembers().get(0).getId().toString())))
                .andExpect(jsonPath("$.members[0].username", is(chatResponse.getMembers().get(0).getUsername())))
                .andExpect(status().isOk());

        verify(chatService, times(1)).getChatById(uuidArgumentCaptor.capture());
        assertThat(chatResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());

    }

    @Test
    void getChatsByMemberId() throws Exception {
        when(chatService.getChatsByUserId(any())).thenReturn(List.of(chatResponse));

        mockMvc.perform(get(ChatController.SOCKET_CHAT_PATH + "/members/" + chat.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(chat.getId().toString())))
                .andExpect(jsonPath("$[0].recentMessage.text", is(messageResponse.getText())))
                .andExpect(jsonPath("$[0].members.size()", is(chatResponse.getMembers().size())))
                .andExpect(jsonPath("$[0].members[0].id", is(chatResponse.getMembers().get(0).getId().toString())))
                .andExpect(jsonPath("$[0].members[0].username", is(chatResponse.getMembers().get(0).getUsername())))
                .andExpect(status().isOk());

        verify(chatService, times(1)).getChatsByUserId(uuidArgumentCaptor.capture());
        assertThat(chatResponse.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void createChat() throws Exception {
        when(chatService.createChat(any())).thenReturn(chatResponse);
        mockMvc.perform(post(ChatController.SOCKET_CHAT_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(jsonPath("$.id", is(chat.getId().toString())))
                .andExpect(jsonPath("$.recentMessage.text", is(messageResponse.getText())))
                .andExpect(jsonPath("$.members.size()", is(chatResponse.getMembers().size())))
                .andExpect(jsonPath("$.members[0].id", is(chatResponse.getMembers().get(0).getId().toString())))
                .andExpect(jsonPath("$.members[0].username", is(chatResponse.getMembers().get(0).getUsername())))
                .andExpect(status().isOk());

        verify(chatService).createChat(chatRequestArgumentCaptor.capture());
        assertThat(chatRequest.getUserIds().get(0)).isEqualTo(chatRequestArgumentCaptor.getValue().getUserIds().get(0));
    }

    @Test
    void addMemberWithId() throws Exception {
        when(chatService.addMemberWithId(any(UUID.class), any(UUID.class))).thenReturn(chatResponse);
        mockMvc.perform(patch(ChatController.SOCKET_CHAT_PATH + "/" + chat.getId() + "/members/add/" + user1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(chat.getId().toString())))
                .andExpect(jsonPath("$.recentMessage.text", is(messageResponse.getText())))
                .andExpect(jsonPath("$.members.size()", is(chatResponse.getMembers().size())))
                .andExpect(jsonPath("$.members[0].id", is(chatResponse.getMembers().get(0).getId().toString())))
                .andExpect(jsonPath("$.members[0].username", is(chatResponse.getMembers().get(0).getUsername())))
                .andExpect(status().isOk());

        verify(chatService).addMemberWithId(user1.getId(), chat.getId());
    }

    @Test
    void removeMemberWithId() throws Exception {
        mockMvc.perform(patch(ChatController.SOCKET_CHAT_PATH + "/" + chat.getId() + "/members/remove/" + user1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(chatService).removeMemberWithId(user1.getId(), chat.getId());
    }

    @Test
    void deleteChatWithId() throws Exception {
        mockMvc.perform(delete(ChatController.SOCKET_CHAT_PATH + "/" + chat.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(chatService).deleteChatById(chat.getId());
    }
}