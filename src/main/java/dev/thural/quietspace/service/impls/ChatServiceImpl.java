package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.CustomErrorException;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.custom.ChatMapper;
import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.ChatService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserService userService;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;


    @Override
    public List<ChatResponse> getChatsByUserId(UUID memberId) {

        User loggedUser = getLoggedUser();

        if (!loggedUser.getId().equals(memberId))
            throw new UnauthorizedException("user mismatch with the chat member");

        return chatRepository.findAllByUsersId(memberId)
                .stream()
                .map(chatMapper::chatEntityToResponse)
                .toList();
    }


    @Override
    public void deleteChatById(UUID chatId) {
        getChatById(chatId);
        if (chatRepository.existsById(chatId)) chatRepository.deleteById(chatId);
    }


    @Override
    public ChatResponse addMemberWithId(UUID memberId, UUID chatId) {

        Chat foundChat = findChatById(chatId);
        User foundMember = getUserById(memberId);
        List<User> members = foundChat.getUsers();

        members.add(foundMember);
        foundChat.setUsers(members);
        Chat patchedChat = chatRepository.save(foundChat);

        return chatMapper.chatEntityToResponse(patchedChat);
    }


    @Override
    public void removeMemberWithId(UUID memberId, UUID chatId) {

        Chat foundChat = findChatById(chatId);
        User foundMember = getUserById(memberId);
        List<User> members = foundChat.getUsers();

        members.remove(foundMember);
        foundChat.setUsers(members);
        chatRepository.save(foundChat);

    }


    @Override
    public ChatResponse createChat(ChatRequest chatRequest) {

        List<User> userList = userService.getUsersFromIdList(chatRequest.getUserIds());
        User loggedUser = getLoggedUser();

        if (!userList.contains(loggedUser))
            throw new UnauthorizedException("requesting user is not member of requested chat");

        boolean isChatDuplicate = chatRepository.findAllByUsersIn(userList).stream()
                .anyMatch(chat -> new HashSet<>(chat.getUsers()).containsAll(userList));

        if (isChatDuplicate) throw new CustomErrorException("a chat with same members already exists");

        Chat createdChat = chatRepository.save(chatMapper.chatRequestToEntity(chatRequest));
        return chatMapper.chatEntityToResponse(createdChat);

    }


    @Override
    public ChatResponse getChatById(UUID chatId) {
        Chat foundChat = findChatById(chatId);
        return chatMapper.chatEntityToResponse(foundChat);
    }


    private User getUserById(UUID memberId) {
        return userRepository
                .findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }


    private User getLoggedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository
                .findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }


    private Chat findChatById(UUID chatId) {
        User loggedUser = getLoggedUser();

        Chat foundChat = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new UnauthorizedException("chat does not belong to logged user");

        return foundChat;
    }

}
