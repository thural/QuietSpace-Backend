package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.MessageMapperImpl;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.repository.MessageRepository;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private UserService userService;

    @Spy
    private MessageMapperImpl messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;


    private User user;
    private Chat chat;
    private Message message;
    private MessageRequest messageRequest;


    @BeforeEach
    void setUp() {
        this.user = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .email("user@email.com")
                .role("admin")
                .password("pAsSword")
                .build();

        this.chat = Chat.builder()
                .id(UUID.randomUUID())
                .users(List.of())
                .messages(List.of())
                .build();

        this.message = Message.builder()
                .id(UUID.randomUUID())
                .sender(user)
                .chat(chat)
                .text("sample text")
                .build();

        this.messageRequest = MessageRequest.builder()
                .senderId(user.getId())
                .text("sample text")
                .chatId(chat.getId())
                .build();
    }

    @Test
    void testAddMessage() {
        when(userService.getLoggedUser()).thenReturn(user);
        when(chatRepository.findById(messageRequest.getChatId())).thenReturn(Optional.of(chat));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse savedMessage = messageService.addMessage(messageRequest);
        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getSenderId()).isEqualTo(user.getId());
        assertThat(savedMessage.getChatId()).isEqualTo(chat.getId());

        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testDeleteMessage() {
        when(userService.getLoggedUser()).thenReturn(user);
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        messageService.deleteMessage(message.getId());

        verify(messageRepository, times(1)).findById(message.getId());
        verify(messageRepository, times(1)).deleteById(message.getId());
    }

    @Test
    void testGetMessagesByChatId() {
        PageRequest pageRequest = buildPageRequest(1, 50, null);

        when(messageRepository.findAllByChatId(chat.getId(), pageRequest)).thenReturn(Page.empty());
        Page<MessageResponse> messagePage = messageService.getMessagesByChatId(1, 50, chat.getId());
        assertThat(messagePage.getContent()).isEmpty();

        verify(messageRepository, times(1)).findAllByChatId(chat.getId(), pageRequest);
    }

    @Test
    void testGetLastMessageByChat() {
        when(messageRepository.findFirstByChatOrderByCreateDateDesc(chat)).thenReturn(Optional.ofNullable(message));

        Optional<MessageResponse> messageResponse = messageService.getLastMessageByChat(chat);

        assertThat(messageResponse).isPresent();
        assertThat(messageResponse.get()).isInstanceOf(MessageResponse.class);
    }

}