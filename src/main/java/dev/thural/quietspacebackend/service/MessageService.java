package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    Page<MessageDTO> getChat(UUID firstUserId, UUID secondUserId, Integer pageNumber, Integer pageSize);

    MessageDTO addOne(MessageDTO messageDTO, String jwtToken);

    Optional<MessageDTO> getById(UUID messageId);

    void updateOne(UUID messageId, MessageDTO messageDTO, String jwtToken);

    void deleteOne(UUID id, String jwtToken);

    void patchOne(UUID messageId, MessageDTO messageDTO, String jwtToken);
}
