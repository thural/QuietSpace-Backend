package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDTO;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    MessageDTO addOne(MessageDTO messageDTO, String authHeader);

    Optional<MessageDTO> getById(UUID messageId);

    void updateOne(UUID messageId, MessageDTO messageDTO, String authHeader);

    void deleteOne(UUID id, String authHeader);

    void patchOne(UUID messageId, MessageDTO messageDTO, String authHeader);
}
