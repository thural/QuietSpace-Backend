package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDto;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    MessageDto addMessage(MessageDto messageDTO, String authHeader);

    Optional<MessageDto> getMessageById(UUID messageId);

    void deleteMessage(UUID id, String authHeader);

    void patchMessage(UUID messageId, MessageDto messageDTO, String authHeader);
}
