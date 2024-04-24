package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.request.MessageRequest;
import dev.thural.quietspacebackend.model.response.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MessageService {

    MessageResponse addMessage(MessageRequest messageRequest);

    void deleteMessage(UUID id);

    Page<MessageResponse> getMessagesByChatId(Integer pageNumber, Integer pageSiz, UUID chatId);
}
