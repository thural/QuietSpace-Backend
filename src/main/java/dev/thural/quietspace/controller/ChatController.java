package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    public static final String CHAT_PATH = "/api/v1/chats";
    private final ChatService chatService;

    @GetMapping("/{chatId}")
    ResponseEntity<ChatResponse> getSingleChatById(@PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }

    @GetMapping("/members/{userId}")
    ResponseEntity<List<ChatResponse>> getChatsByMemberId(@PathVariable UUID userId) {
        return ResponseEntity.ok(chatService.getChatsByUserId(userId));
    }

    @PostMapping()
    ResponseEntity<ChatResponse> createChat(@RequestBody ChatRequest chat) {
        return ResponseEntity.ok(chatService.createChat(chat));
    }

    @PatchMapping("/{chatId}/members/add/{userId}")
    ResponseEntity<UserResponse> addMemberWithId(@PathVariable UUID userId, @PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.addMemberWithId(userId, chatId));
    }

    @PatchMapping("/{chatId}/members/remove/{userId}")
    ResponseEntity<?> removeMemberWithId(@PathVariable UUID chatId, @PathVariable UUID userId) {
        chatService.removeMemberWithId(userId, chatId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{chatId}")
    ResponseEntity<?> deleteChatWithId(@PathVariable("chatId") UUID chatId) {
        chatService.deleteChatById(chatId);
        return ResponseEntity.noContent().build();
    }

}
