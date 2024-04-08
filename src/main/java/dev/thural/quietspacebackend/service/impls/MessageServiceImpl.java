package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.MessageMapper;
import dev.thural.quietspacebackend.model.MessageDto;
import dev.thural.quietspacebackend.repository.ChatRepository;
import dev.thural.quietspacebackend.repository.MessageRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static dev.thural.quietspacebackend.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Override
    public MessageDto addMessage(MessageDto messageDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        ChatEntity parentChat = chatRepository.findById(messageDTO.getChatId())
                .orElseThrow(EntityNotFoundException::new);

        MessageEntity newMessage = messageMapper.messageDtoToEntity(messageDTO);

        newMessage.setSender(loggedUser);
        newMessage.setChat(parentChat);

        return messageMapper.messageEntityToDto(messageRepository.save(newMessage));
    }

    @Override
    public void deleteMessage(UUID messageId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        MessageEntity existingMessage = messageRepository.findById(messageId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingMessage.getSender().getId().equals(loggedUser.getId())) {
            messageRepository.deleteById(messageId);
        } else throw new AccessDeniedException("message does not belong to current user");
    }

    @Override
    public Page<MessageDto> getMessagesByChatId(Integer pageNumber, Integer pageSize, UUID chatId) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        Page<MessageEntity> messagePage = messageRepository.findAllByChatId(chatId, pageRequest);
        return messagePage.map(messageMapper::messageEntityToDto);
    }

}
