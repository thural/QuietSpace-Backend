package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDto;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    MessageDto addMessage(MessageDto messageDTO, String authHeader);

    void deleteMessage(UUID id, String authHeader);

}
