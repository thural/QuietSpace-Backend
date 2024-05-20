package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    MessageResponse addMessage(MessageRequest messageRequest);

    void deleteMessage(UUID id);

    Page<MessageResponse> getMessagesByChatId(Integer pageNumber, Integer pageSize, UUID chatId);

    Optional<MessageResponse> getLastMessageByChat(Chat chat);

}
