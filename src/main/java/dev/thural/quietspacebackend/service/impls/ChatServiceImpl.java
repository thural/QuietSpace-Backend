package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.ChatMapper;
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
    private final ChatMapper chatMapper;
    private final JwtProvider jwtProvider;

    @Override
    public List<ChatDTO> getChatsByOwnerId(UUID ownerId, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        if(loggedUser == null)
            throw new NotFoundException("user not found");

        if (!loggedUser.getId().equals(ownerId))
            throw new AccessDeniedException("user mismatch with the chat owner");

        return chatRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(chatMapper::chatEntityToDto).toList();
    }

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

        if (!loggedUser.equals(foundChat.getOwner()))
            throw new AccessDeniedException("user mismatch with the chat owner");

        if (chatRepository.existsById(chatId)) chatRepository.deleteById(chatId);

    }

    @Override
    public void addMemberWithId(UUID memberId, UUID chatId, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken).orElse(null);

        UserEntity foundMember = userRepository.findById(memberId)
                .orElseThrow(NotFoundException::new);

        ChatEntity foundChat = chatRepository.findById(chatId)
                .orElseThrow(NotFoundException::new);

        if (!foundChat.getOwner().equals(loggedUser))
            throw new AccessDeniedException("logged user is not the owner of the chat");

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

        if (!foundChat.getOwner().equals(loggedUser))
            throw new AccessDeniedException("logged user is not the owner of the chat");

        List<UserEntity> members = foundChat.getUsers();
        members.remove(foundMember);
        foundChat.setUsers(members);

        chatRepository.save(foundChat);

    }

    @Override
    public void createChat(ChatDTO chatDTO, String jwtToken) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(jwtToken)
                .orElseThrow(NotFoundException::new);
        UserEntity foundUser = userRepository.findById(chatDTO.getOwnerId())
                .orElseThrow(NotFoundException::new);

        if (!loggedUser.equals(foundUser))
            throw new AccessDeniedException("logged user is not the owner of the chat");

        ChatEntity newChat = chatMapper.chatDtoToEntity(chatDTO);
        newChat.setOwner(foundUser);

        chatRepository.save(newChat);

    }

}
