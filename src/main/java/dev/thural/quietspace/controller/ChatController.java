package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{chatId}")
    ChatResponse getSingleChatById(@PathVariable UUID chatId) {
        return chatService.getChatById(chatId);
    }

    @GetMapping("/members/{userId}")
    List<ChatResponse> getChatsByMemberId(@PathVariable UUID userId) {
        return chatService.getChatsByUserId(userId);
    }

    @PostMapping()
    ResponseEntity<?> createChat(@RequestBody ChatRequest chat) {
        chatService.createChat(chat);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/{chatId}/members/add/{userId}")
    ResponseEntity<?> addMemberWithId(@PathVariable UUID chatId, @PathVariable UUID userId) {
        chatService.addMemberWithId(userId, chatId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/{chatId}/members/remove/{userId}")
    ResponseEntity<?> removeMemberWithId(@PathVariable UUID chatId, @PathVariable UUID userId) {
        chatService.removeMemberWithId(userId, chatId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{chatId}")
    ResponseEntity<?> deleteChatWithId(@PathVariable("chatId") UUID chatId) {
        chatService.deleteChatById(chatId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
