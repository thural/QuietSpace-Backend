package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    List<ChatResponse> getChatsByUserId(UUID userId);

    void deleteChatById(UUID chatId);

    UserResponse addMemberWithId(UUID memberId, UUID chatId);

    List<UserResponse> removeMemberWithId(UUID memberId, UUID chatId);

    ChatResponse createChat(ChatRequest chatRequest);

    ChatResponse getChatById(UUID chatId);

}
