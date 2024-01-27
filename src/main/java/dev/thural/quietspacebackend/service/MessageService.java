package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDTO;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    MessageDTO addMessage(MessageDTO messageDTO, String authHeader);

    Optional<MessageDTO> getMessageById(UUID messageId);

    void deleteMessage(UUID id, String authHeader);

    void patchMessage(UUID messageId, MessageDTO messageDTO, String authHeader);
}
