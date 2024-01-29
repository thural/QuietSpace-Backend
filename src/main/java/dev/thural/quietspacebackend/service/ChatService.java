package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.ChatDto;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatDto> getChatsByUserId(UUID userId, String authHeader);

    void deleteChatById(UUID chatId, String authHeader);

    void addMemberWithId(UUID memberId, UUID chatId, String authHeader);

    void removeMemberWithId(UUID memberId, UUID chatId, String authHeader);

    ChatDto createChat(ChatDto chatDTO, String authHeader);

    ChatDto getChatById(UUID chatId, String authHeader);
}
