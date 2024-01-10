package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.MessageMapper;
import dev.thural.quietspacebackend.model.MessageDTO;
import dev.thural.quietspacebackend.repository.ChatRepository;
import dev.thural.quietspacebackend.repository.MessageRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.MessageService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @Override
    public Page<MessageDTO> getChat(UUID firstUserId, UUID secondUserId, Integer pageNumber, Integer pageSize) {
        return null;
    }

    @Override
    public MessageDTO addOne(MessageDTO messageDTO, String jwtToken) {
        UserEntity sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(NotFoundException::new);

        ChatEntity parentChat = chatRepository.findById(messageDTO.getChatId())
                .orElseThrow(NotFoundException::new);

        MessageEntity newMessage = messageMapper.messageDtoToEntity(messageDTO);
        newMessage.setSender(sender);

        newMessage.setChat(parentChat);

        System.out.println("message entity: " + newMessage );

        return messageMapper.messageEntityToDto(messageRepository.save(newMessage));
    }

    @Override
    public Optional<MessageDTO> getById(UUID messageId) {
        return null;
    }

    @Override
    public void updateOne(UUID messageId, MessageDTO messageDTO, String jwtToken) {

    }

    @Override
    public void deleteOne(UUID messageId, String jwtToken) {
        UserEntity loggedUserEntity = getUserEntityByToken(jwtToken);

        MessageEntity existingMessage = messageRepository.findById(messageId).orElseThrow(NotFoundException::new);

        if (existingMessage.getSender().getId().equals(loggedUserEntity.getId())) {
            messageRepository.deleteById(messageId);
        } else throw new AccessDeniedException("message does not belong to current user");
    }

    @Override
    public void patchOne(UUID messageId, MessageDTO messageDTO, String jwtToken) {

    }

    private UserEntity getUserEntityByToken(String jwtToken) {
        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(jwtToken);
        return userRepository.findUserEntityByEmail(loggedUserEmail).orElseThrow(NotFoundException::new);
    }
}
