package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.Chat;
import dev.thural.quietspacebackend.entity.Message;
import dev.thural.quietspacebackend.entity.User;
import dev.thural.quietspacebackend.exception.CustomErrorException;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.ChatMapper;
import dev.thural.quietspacebackend.mapper.MessageMapper;
import dev.thural.quietspacebackend.model.request.ChatRequest;
import dev.thural.quietspacebackend.model.response.ChatResponse;
import dev.thural.quietspacebackend.repository.ChatRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;


    @Override
    public List<ChatResponse> getChatsByUserId(UUID memberId) {
        User loggedUser = getLoggedUser();

        if (!loggedUser.getId().equals(memberId))
            throw new AccessDeniedException("user mismatch with the chat member");

        return chatRepository.findAllByUsersId(memberId)
                .stream()
                .map(chatMapper::chatEntityToResponse).toList();
    }

    @Override
    public void deleteChatById(UUID chatId) {
        getChatById(chatId);
        if (chatRepository.existsById(chatId)) chatRepository.deleteById(chatId);
    }

    @Override
    public void addMemberWithId(UUID memberId, UUID chatId) {

        Chat foundChat = findChatById(chatId);

        User foundMember = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        List<User> members = foundChat.getUsers();
        members.add(foundMember);
        foundChat.setUsers(members);
        chatRepository.save(foundChat);
    }

    @Override
    public void removeMemberWithId(UUID memberId, UUID chatId) {
        Chat foundChat = findChatById(chatId);

        User foundMember = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        List<User> members = foundChat.getUsers();

        members.remove(foundMember);
        foundChat.setUsers(members);
        chatRepository.save(foundChat);
    }

    @Override
    public ChatResponse createChat(ChatRequest chatRequest) {
        List<User> userList = chatRequest.getUserIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("user not found"))).toList();

        User loggedUser = getLoggedUser();

        if (!userList.contains(loggedUser))
            throw new AccessDeniedException("logged user is not a member of requested chat");

        boolean isChatDuplicate = chatRepository.findAllByUsersIn(userList)
                .stream().anyMatch(chat -> new HashSet<>(chat.getUsers()).containsAll(userList));

        if (isChatDuplicate) throw new CustomErrorException("a chat with same members already exists");

        Chat newChat = chatMapper.chatRequestToEntity(chatRequest);
        newChat.setUsers(userList);
        Message message = messageMapper.messageRequestToEntity(chatRequest.getMessage());
        newChat.setMessages(List.of());

        return chatMapper.chatEntityToResponse(chatRepository.save(newChat));
    }

    private User getLoggedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }

    @Override
    public ChatResponse getChatById(UUID chatId) {
        User loggedUser = getLoggedUser();

        Chat foundChat = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        return chatMapper.chatEntityToResponse(foundChat);
    }

    private Chat findChatById(UUID chatId) {
        User loggedUser = getLoggedUser();

        Chat foundChat = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        return foundChat;
    }

}
