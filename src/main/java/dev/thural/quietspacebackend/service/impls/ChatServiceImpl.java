package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.ChatMapper;
import dev.thural.quietspacebackend.model.ChatDto;
import dev.thural.quietspacebackend.repository.ChatRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.ChatService;
import dev.thural.quietspacebackend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ChatMapper chatMapper;


    @Override
    public List<ChatDto> getChatsByUserId(UUID memberId, String authHeader) {
        UserEntity loggedUser = userService.findUserByJwt(authHeader).orElse(null);

        if (loggedUser == null)
            throw new UserNotFoundException("user not found");

        if (!loggedUser.getId().equals(memberId))
            throw new AccessDeniedException("user mismatch with the chat member");

        return chatRepository.findAllByUsersId(memberId)
                .stream()
                .map(chatMapper::chatEntityToDto).toList();
    }

    @Override
    public void deleteChatById(UUID chatId, String authHeader) {
        UserEntity loggedUser = userService.findUserByJwt(authHeader).orElse(null);

        if (loggedUser == null)
            throw new UserNotFoundException("user not found");

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        if (chatRepository.existsById(chatId)) chatRepository.deleteById(chatId);
    }

    @Override
    public void addMemberWithId(UUID memberId, UUID chatId, String authHeader) {
        UserEntity loggedUser = userService.findUserByJwt(authHeader).orElse(null);

        UserEntity foundMember = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        List<UserEntity> members = foundChat.getUsers();
        members.add(foundMember);
        foundChat.setUsers(members);

        chatRepository.save(foundChat);
    }

    @Override
    public void removeMemberWithId(UUID memberId, UUID chatId, String authHeader) {
        UserEntity loggedUser = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new AccessDeniedException("logged user does not exist"));

        UserEntity foundMember = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        List<UserEntity> members = foundChat.getUsers();
        members.remove(foundMember);
        foundChat.setUsers(members);

        chatRepository.save(foundChat);
    }

    @Override
    public ChatDto createChat(ChatDto chatDTO, String authHeader) {
        List<UserEntity> userList = chatDTO.getUserIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("user not found"))
                ).toList();

        UserEntity loggedUser = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (!userList.contains(loggedUser))
            throw new AccessDeniedException("logged user is not a member of requested chat");

        boolean isChatDuplicate = chatRepository.findAllByUsersIn(userList)
                .stream().anyMatch(chat -> new HashSet<>(chat.getUsers()).containsAll(userList));

        if (isChatDuplicate) throw new RuntimeException("a chat with same members already exists");

        ChatEntity newChat = chatMapper.chatDtoToEntity(chatDTO);
        newChat.setUsers(userList);

        return chatMapper.chatEntityToDto(chatRepository.save(newChat));
    }

    @Override
    public ChatDto getChatById(UUID chatId, String authHeader) {
        UserEntity loggedUser = userService.findUserByJwt(authHeader).orElse(null);

        if (loggedUser == null)
            throw new UserNotFoundException("user not found");

        ChatEntity foundChatEntity = chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);

        if (!foundChatEntity.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        return chatMapper.chatEntityToDto(foundChatEntity);
    }

}
