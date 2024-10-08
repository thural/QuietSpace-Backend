package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.mapper.custom.ChatMapper;
import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatMapper chatMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private UUID userId;
    private UUID memberId;
    private User user1;
    private UserResponse userResponse;
    private List<User> userList;
    private User user2;
    private Chat chat;
    private ChatResponse chatResponse;
    private ChatRequest chatRequest;

    @BeforeEach
    void initMockData() {
        this.userId = UUID.randomUUID();
        this.memberId = UUID.randomUUID();

        this.user1 = User.builder()
                .id(userId)
                .username("user")
                .email("user@email.com")
                .password("pAsSword")
                .build();

        this.userResponse = UserResponse.builder()
                .id(user1.getId())
                .build();

        this.user2 = User.builder()
                .id(userId)
                .username("member")
                .email("member@email.com")
                .password("pAsSWord")
                .build();

        this.userList = new ArrayList<>();
        userList.add(user1);

        this.chat = Chat.builder()
                .id(UUID.randomUUID())
                .users(userList)
                .messages(List.of())
                .build();

        this.chatRequest = ChatRequest.builder()
                .userIds(List.of(userId, memberId))
                .build();

        this.chatResponse = new ChatResponse();
    }

    @Test
    void testFindChatById() {
        when(userService.getSignedUser()).thenReturn(user1);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        Chat foundChat = chatService.findChatEntityById(chat.getId());

        assertThat(foundChat).isEqualTo(chat);
        verify(userService, times(1)).getSignedUser();
        verify(chatRepository, times(1)).findById(chat.getId());
    }

    @Test
    void testGetChatsByUserId() {
        when(userService.getSignedUser()).thenReturn(user1);
        when(chatRepository.findAllByUsersId(userId)).thenReturn(List.of(chat));
        when(chatMapper.chatEntityToResponse(any(Chat.class))).thenReturn(chatResponse);

        List<ChatResponse> chats = chatService.getChatsByUserId(userId);

        assertThat(chats).isEqualTo(List.of(chatResponse));
        verify(userService, times(1)).getSignedUser();
        verify(chatMapper, times(1)).chatEntityToResponse(any(Chat.class));
        verify(chatRepository, times(1)).findAllByUsersId(userId);
    }

    @Test
    void testDeleteChatById() {
        when(userService.getSignedUser()).thenReturn(user1);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        chatService.deleteChatById(chat.getId());

        verify(userService, times(1)).getSignedUser();
        verify(chatRepository, times(1)).deleteById(chat.getId());
    }

    @Test
    void testAddMember() {
        UserResponse memberResponse = new UserResponse();
        BeanUtils.copyProperties(user2, memberResponse);

        when(userService.getSignedUser()).thenReturn(user1);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(userService.getUserById(memberId)).thenReturn(Optional.of(user2));
        when(userMapper.toResponse(user2)).thenReturn(memberResponse);

        UserResponse addedUser = chatService.addMemberWithId(memberId, chat.getId());
        assertThat(addedUser).isEqualTo(memberResponse);
        verify(userMapper, times(1)).toResponse(user2);
    }

    @Test
    void testRemoveMember() {
        when(userService.getSignedUser()).thenReturn(user1);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(userService.getUserById(userId)).thenReturn(Optional.of(user1));
        when(chatRepository.save(any(Chat.class))).thenReturn(chat);

        chatService.removeMemberWithId(userId, chat.getId());

        verify(userService, times(1)).getSignedUser();
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    void testCreateChat() {
        when(userService.getSignedUser()).thenReturn(user1);
        when(userService.getUsersFromIdList(anyList())).thenReturn(userList);
        when(chatRepository.findAllByUsersIn(userList)).thenReturn(List.of());
        when(chatMapper.chatEntityToResponse(chat)).thenReturn(chatResponse);
        when(chatMapper.chatRequestToEntity(chatRequest)).thenReturn(chat);
        when(chatRepository.save(chat)).thenReturn(chat);

        ChatResponse createdChat = chatService.createChat(chatRequest);

        assertThat(createdChat).isEqualTo(chatResponse);
        verify(chatRepository, times(1)).findAllByUsersIn(userList);
        verify(chatRepository, times(1)).save(chat);
    }

}