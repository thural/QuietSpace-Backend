package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.ChatMapper;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.ChatDTO;
import dev.thural.quietspacebackend.repository.ChatRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.ChatService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ChatMapper chatMapper;
    private final JwtProvider jwtProvider;


    @Override
    public List<ChatDTO> getChatsByUserId(UUID memberId, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        if(loggedUser == null)
            throw new NotFoundException("user not found");

        if (!loggedUser.getId().equals(memberId))
            throw new AccessDeniedException("user mismatch with the chat member");

        return chatRepository.findAllByUsersId(memberId)
                .stream()
                .map(chatMapper::chatEntityToDto).toList();
    }

    @Override
    public void deleteChatById(UUID chatId, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        if(loggedUser == null)
            throw new NotFoundException("user not found");

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(NotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        if (chatRepository.existsById(chatId)) chatRepository.deleteById(chatId);

    }

    @Override
    public void addMemberWithId(UUID memberId, UUID chatId, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        UserEntity foundMember = userRepository.findById(memberId)
                .orElseThrow(NotFoundException::new);

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(NotFoundException::new);


        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        List<UserEntity> members = foundChat.getUsers();
        members.add(foundMember);
        foundChat.setUsers(members);

        chatRepository.save(foundChat);

    }

    @Override
    public void removeMemberWithId(UUID memberId, UUID chatId, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        UserEntity foundMember = userRepository.findById(memberId)
                .orElseThrow(NotFoundException::new);

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(NotFoundException::new);

        if (!foundChat.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        List<UserEntity> members = foundChat.getUsers();
        members.remove(foundMember);
        foundChat.setUsers(members);

        chatRepository.save(foundChat);

    }

    @Override
    public ChatDTO createChat(ChatDTO chatDTO, String jwtToken) {
        List<UserEntity> userList = chatDTO.getUsers().stream()
                .map(user -> userRepository.findById(user.getId())
                        .orElseThrow(NotFoundException::new))
                .toList();

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken)
                .orElseThrow(NotFoundException::new);

        System.out.println("logged user: " + loggedUser);
        System.out.println("chat user list: " + userList);

        if (!userList.contains(loggedUser))
            throw new AccessDeniedException("logged user is not a member of requested chat");

        boolean isChatDuplicate = chatRepository.findAllByUsers(userList.get(0))
                .stream().anyMatch(chat -> chat.getUsers().contains(userList.get(0)));

        if (isChatDuplicate) throw new RuntimeException("a chat with same members already exists");

        ChatEntity newChat = chatMapper.chatDtoToEntity(chatDTO);
        newChat.setUsers(userList);

        return chatMapper.chatEntityToDto(chatRepository.save(newChat));
    }

    @Override
    public ChatDTO getChatById(UUID chatId, String jwtToken) {
        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        if(loggedUser == null)
            throw new NotFoundException("user not found");

        ChatEntity foundChatEntity = chatRepository.findById(chatId).orElseThrow(NotFoundException::new);

        if (!foundChatEntity.getUsers().contains(loggedUser))
            throw new AccessDeniedException("chat does not belong to logged user");

        return chatMapper.chatEntityToDto(foundChatEntity);
    }

}
