package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.ChatDTO;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatDTO> getChatsByUserId(UUID userId, String authHeader);

    void deleteChatById(UUID chatId, String authHeader);

    void addMemberWithId(UUID memberId, UUID chatId, String authHeader);

    void removeMemberWithId(UUID memberId, UUID chatId, String authHeader);

    ChatDTO createChat(ChatDTO chatDTO, String authHeader);

    ChatDTO getChatById(UUID chatId, String authHeader);
}
