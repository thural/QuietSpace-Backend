package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.MessageDto;

import java.util.UUID;

public interface MessageService {

    MessageDto addMessage(MessageDto messageDTO);

    void deleteMessage(UUID id);

}
