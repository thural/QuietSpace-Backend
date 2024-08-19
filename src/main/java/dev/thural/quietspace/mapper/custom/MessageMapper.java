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
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    public Message toEntity(MessageRequest message) {
        return Message.builder()
                .text(message.getText())
                .chat(findChatById(message.getChatId()))
                .sender(findUserById(message.getSenderId()))
                .recipient(findUserById(message.getRecipientId()))
                .build();
    }

    public MessageResponse toResponse(Message message) {
        return MessageResponse
                .builder()
                .text(message.getText())
                .id(message.getId())
                .isSeen(message.getIsSeen())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .senderName(message.getSender().getName())
                .build();
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
