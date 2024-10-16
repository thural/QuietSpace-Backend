package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.custom.MessageMapper;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.repository.MessageRepository;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper messageMapper;
    private final UserService userService;

    @Override
    public MessageResponse addMessage(MessageRequest messageRequest) {
        User loggedUser = userService.getSignedUser();
        Chat parentChat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(EntityNotFoundException::new);

        Message newMessage = messageMapper.toEntity(messageRequest);
        newMessage.setSender(loggedUser);
        newMessage.setChat(parentChat);
        return messageMapper.toResponse(messageRepository.save(newMessage));
    }

    @Override
    public Optional<MessageResponse> deleteMessage(UUID messageId) {
        Message existingMessage = findMessageOrElseThrow(messageId);
        checkResourceAccess(existingMessage.getSender().getId());

        messageRepository.deleteById(messageId);
        return Optional.of(messageMapper.toResponse(existingMessage));
    }

    private Message findMessageOrElseThrow(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(EntityNotFoundException::new);
    }

    private void checkResourceAccess(UUID userId) {
        User loggedUser = userService.getSignedUser();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("message does not belong to current user");
    }

    @Override
    public Page<MessageResponse> getMessagesByChatId(Integer pageNumber, Integer pageSize, UUID chatId) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        Page<Message> messagePage = messageRepository.findAllByChatId(chatId, pageRequest);
        return messagePage.map(messageMapper::toResponse);
    }

    @Override
    public Optional<MessageResponse> getLastMessageByChat(Chat chat) {
        return messageRepository.findFirstByChatOrderByCreateDateDesc(chat)
                .map(messageMapper::toResponse);
    }

    @Override
    public Optional<MessageResponse> setMessageSeen(UUID messageId) {
        Message existingMessage = findMessageOrElseThrow(messageId);
        existingMessage.setIsSeen(true);

        Message savedMessage = messageRepository.save(existingMessage);
        return Optional.ofNullable(messageMapper.toResponse(savedMessage));
    }

}