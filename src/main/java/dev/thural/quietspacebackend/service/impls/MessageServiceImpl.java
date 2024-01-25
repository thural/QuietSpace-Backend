package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.MessageMapper;
import dev.thural.quietspacebackend.model.MessageDTO;
import dev.thural.quietspacebackend.repository.ChatRepository;
import dev.thural.quietspacebackend.repository.MessageRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.MessageService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final JwtProvider jwtProvider;

    @Override
    public MessageDTO addOne(MessageDTO messageDTO, String authHeader) {

        UserEntity loggedUser = jwtProvider.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        ChatEntity parentChat = chatRepository.findById(messageDTO.getChatId())
                .orElseThrow(EntityNotFoundException::new);

        MessageEntity newMessage = messageMapper.messageDtoToEntity(messageDTO);

        newMessage.setSender(loggedUser);
        newMessage.setChat(parentChat);

        return messageMapper.messageEntityToDto(messageRepository.save(newMessage));
    }

    @Override
    public Optional<MessageDTO> getById(UUID messageId) {
        return Optional.empty();
    }

    @Override
    public void updateOne(UUID messageId, MessageDTO messageDTO, String authHeader) {

    }

    @Override
    public void deleteOne(UUID messageId, String authHeader) {
        UserEntity loggedUserEntity = getUserEntityByToken(authHeader);

        MessageEntity existingMessage = messageRepository.findById(messageId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingMessage.getSender().getId().equals(loggedUserEntity.getId())) {
            messageRepository.deleteById(messageId);
        } else throw new AccessDeniedException("message does not belong to current user");
    }

    @Override
    public void patchOne(UUID messageId, MessageDTO messageDTO, String authHeader) {

    }

    private UserEntity getUserEntityByToken(String jwtToken) {
        String loggedUserEmail = JwtProvider.extractEmailFromHeaderToken(jwtToken);
        return userRepository.findUserEntityByEmail(loggedUserEmail)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }
}
