package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MessageService {

    MessageDto addMessage(MessageDto messageDTO);

    void deleteMessage(UUID id);

    Page<MessageDto> getMessagesByChatId(Integer pageNumber, Integer pageSiz, UUID chatId);
}
