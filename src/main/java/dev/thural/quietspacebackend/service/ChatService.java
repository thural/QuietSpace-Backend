package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.ChatDTO;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    List<ChatDTO> getChatsByOwnerId(UUID ownerId, String jwtToken);

    List<ChatDTO> getChatsByMemberId(UUID ownerId, String jwtToken);

    void deleteChatById(UUID chatId, String jwtToken);

    void addMemberWithId(UUID memberId, UUID chatId, String jwtToken);

    void removeMemberWithId(UUID memberId, UUID chatId, String jwtToken);

    void createChat(ChatDTO chatDTO, String jwtToken);
}
