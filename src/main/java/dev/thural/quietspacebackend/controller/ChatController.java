package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.ChatDTO;
import dev.thural.quietspacebackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    public static final String CHAT_PATH = "/api/v1/chats";

    private final ChatService chatService;

    @RequestMapping(value = CHAT_PATH + "/{chatId}", method = RequestMethod.GET)
    ChatDTO getSingleChatById(@RequestHeader("Authorization") String authHeader,
                              @PathVariable("chatId") UUID chatId) {

        return chatService.getChatById(chatId, authHeader);
    }

    @RequestMapping(value = CHAT_PATH + "/member/{userId}", method = RequestMethod.GET)
    List<ChatDTO> getAllChatsByMemberId(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("userId") UUID userId) {
        return chatService.getChatsByUserId(userId, authHeader);
    }

    @RequestMapping(value = CHAT_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createChat(@RequestHeader("Authorization") String authHeader,
                                 @RequestBody ChatDTO chatDTO) {
        ChatDTO createdChatDTO = chatService.createChat(chatDTO, authHeader);
        return new ResponseEntity<>(createdChatDTO, HttpStatus.CREATED);
    }

    @RequestMapping(value = CHAT_PATH + "/{chatId}/member/add/{userId}", method = RequestMethod.PATCH)
    ResponseEntity<?> addMemberWithId(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable("chatId") UUID chatId,
                                      @PathVariable("userId") UUID userId) {
        chatService.addMemberWithId(userId, chatId, authHeader);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = CHAT_PATH + "/{chatId}/member/remove/{userId}", method = RequestMethod.PATCH)
    ResponseEntity<?> removeMemberWithId(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable("chatId") UUID chatId,
                                         @PathVariable("userId") UUID userId) {
        chatService.removeMemberWithId(userId, chatId, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = CHAT_PATH + "/{chatId}", method = RequestMethod.DELETE)
    ResponseEntity<?> deleteChatWithId(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable("chatId") UUID chatId) {
        chatService.deleteChatById(chatId, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
