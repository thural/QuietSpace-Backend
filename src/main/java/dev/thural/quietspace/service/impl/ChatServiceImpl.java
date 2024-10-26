package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.CustomErrorException;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.ChatMapper;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.CreateChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.service.ChatService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserService userService;
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final UserMapper userMapper;


    @Override
    public List<ChatResponse> getChatsByUserId(UUID memberId) {
        User loggedUser = userService.getSignedUser();
        if (!loggedUser.getId().equals(memberId)) throw new UnauthorizedException("user mismatch with the chat member");
        return chatRepository.findAllByUsersId(memberId).stream().map(chatMapper::chatEntityToResponse).toList();
    }

    @Override
    public void deleteChatById(UUID chatId) {
        findChatEntityById(chatId);
        chatRepository.deleteById(chatId);
    }

    @Override
    public UserResponse addMemberWithId(UUID memberId, UUID chatId) {
        Chat foundChat = findChatEntityById(chatId);
        User foundMember = userService.getUserById(memberId).orElseThrow(UserNotFoundException::new);
        List<User> members = foundChat.getUsers();
        members.add(foundMember);
        foundChat.setUsers(members);
        return userMapper.toResponse(foundMember);
    }

    @Override
    public List<UserResponse> removeMemberWithId(UUID memberId, UUID chatId) {
        Chat foundChat = findChatEntityById(chatId);
        User foundMember = getUserById(memberId);
        List<User> members = foundChat.getUsers();
        members.remove(foundMember);
        foundChat.setUsers(members);
        chatRepository.save(foundChat);
        return members.stream().map(userMapper::toResponse).toList();
    }

    @Override
    public ChatResponse createChat(CreateChatRequest chatRequest) {
        List<User> userList = userService.getUsersFromIdList(chatRequest.getUserIds());
        User loggedUser = userService.getSignedUser();
        if (!userList.contains(loggedUser)) throw new UnauthorizedException("requesting user is not member of chat");
        boolean isChatDuplicate = chatRepository.findAllByUsersIn(userList).stream()
                .anyMatch(chat -> new HashSet<>(chat.getUsers()).containsAll(userList));
        if (isChatDuplicate) throw new CustomErrorException("a chat with same members already exists");
        Chat createdChat = chatRepository.save(chatMapper.chatRequestToEntity(chatRequest));
        return chatMapper.chatEntityToResponse(createdChat);

    }

    @Override
    public ChatResponse getChatById(UUID chatId) {
        Chat foundChat = findChatEntityById(chatId);
        return chatMapper.chatEntityToResponse(foundChat);
    }

    private User getUserById(UUID memberId) {
        return userService.getUserById(memberId).orElseThrow(() -> new UserNotFoundException("user not found"));
    }

    public Chat findChatEntityById(UUID chatId) {
        User loggedUser = userService.getSignedUser();
        Chat foundChat = chatRepository.findById(chatId).orElseThrow(EntityNotFoundException::new);
        if (!foundChat.getUsers().contains(loggedUser)) throw new UnauthorizedException("chat user mismatch");
        return foundChat;
    }

}
