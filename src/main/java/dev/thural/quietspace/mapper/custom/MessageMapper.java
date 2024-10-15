package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.repository.ChatRepository;
import dev.thural.quietspace.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    public Message toEntity(MessageRequest request) {
        var message = new Message();
        BeanUtils.copyProperties(request, message);
        message.setChat(findChatById(request.getChatId()));
        message.setSender(findUserById(request.getSenderId()));
        message.setRecipient(findUserById(request.getRecipientId()));
        return message;
    }

    public MessageResponse toResponse(Message message) {
        var response = new MessageResponse();
        BeanUtils.copyProperties(message, response);
        response.setChatId(message.getChat().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getName());
        response.setRecipientId(message.getRecipient().getId());
        return response;
    }

    private Chat findChatById(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(EntityNotFoundException::new);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
