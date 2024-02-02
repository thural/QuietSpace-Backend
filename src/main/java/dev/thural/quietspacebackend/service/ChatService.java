package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.ChatDto;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatDto> getChatsByUserId(UUID userId);

    void deleteChatById(UUID chatId);

    void addMemberWithId(UUID memberId, UUID chatId);

    void removeMemberWithId(UUID memberId, UUID chatId);

    ChatDto createChat(ChatDto chatDTO);

    ChatDto getChatById(UUID chatId);
}
