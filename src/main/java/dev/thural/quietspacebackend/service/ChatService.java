package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.ChatDTO;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatDTO> getChatsByUserId(UUID userId, String jwtToken);

    void deleteChatById(UUID chatId, String jwtToken);

    void addMemberWithId(UUID memberId, UUID chatId, String jwtToken);

    void removeMemberWithId(UUID memberId, UUID chatId, String jwtToken);

    ChatDTO createChat(ChatDTO chatDTO, String jwtToken);

    ChatDTO getChatById(UUID chatId, String jwtToken);
}
