package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.MessageMapper;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.repository.MessageRepository;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper messageMapper;
    private final UserService userService;

    @Override
    public MessageResponse addMessage(MessageRequest messageRequest) {
        User loggedUser = userService.getLoggedUser();

        Chat parentChat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(EntityNotFoundException::new);

        Message newMessage = messageMapper.messageRequestToEntity(messageRequest);

        newMessage.setSender(loggedUser);
        newMessage.setChat(parentChat);

        return messageMapper.messageEntityToDto(messageRepository.save(newMessage));
    }

    @Override
    public void deleteMessage(UUID messageId) {
        User loggedUser = userService.getLoggedUser();

        Message existingMessage = messageRepository.findById(messageId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingMessage.getSender().getId().equals(loggedUser.getId())) {
            messageRepository.deleteById(messageId);
        } else throw new AccessDeniedException("message does not belong to current user");
    }

    @Override
    public Page<MessageResponse> getMessagesByChatId(Integer pageNumber, Integer pageSize, UUID chatId) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize,null);
        Page<Message> messagePage = messageRepository.findAllByChatId(chatId, pageRequest);
        return messagePage.map(messageMapper::messageEntityToDto);
    }

    @Override
    public Optional<MessageResponse> getLastMessageByChat(Chat chat) {
        return messageRepository.findFirstByChatOrderByCreateDateDesc(chat)
                .map(messageMapper::messageEntityToDto);
    }

}
