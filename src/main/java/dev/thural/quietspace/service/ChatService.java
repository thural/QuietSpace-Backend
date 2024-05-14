package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    List<ChatResponse> getChatsByUserId(UUID userId);

    void deleteChatById(UUID chatId);

    void addMemberWithId(UUID memberId, UUID chatId);

    void removeMemberWithId(UUID memberId, UUID chatId);

    void createChat(ChatRequest chatRequest);

    ChatResponse getChatById(UUID chatId);

}
