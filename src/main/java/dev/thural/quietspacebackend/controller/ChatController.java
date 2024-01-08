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

    public static final String CHAT_PATH = "/api/v1/chat";

    private final ChatService chatService;

    @RequestMapping(value = CHAT_PATH + "/owner/{userId}", method = RequestMethod.GET)
    List<ChatDTO> getAllChatsByOwnerId(@RequestHeader("Authorization") String jwtToken,
                                       @PathVariable("userId") UUID userId) {
        return chatService.getChatsByOwnerId(userId, jwtToken);
    }

    @RequestMapping(value = CHAT_PATH + "/member/{userId}", method = RequestMethod.GET)
    List<ChatDTO> getAllChatsByMemberId(@RequestHeader("Authorization") String jwtToken,
                                        @PathVariable("userId") UUID userId) {
        return chatService.getChatsByMemberId(userId, jwtToken);
    }

    @RequestMapping(value = CHAT_PATH + "/{chatId}/member/add/{userId}", method = RequestMethod.POST)
    ResponseEntity addMemberWithId(@RequestHeader("Authorization") String jwtToken,
                                   @PathVariable("chatId") UUID chatId,
                                   @PathVariable("userId") UUID userId) {
        chatService.addMemberWithId(userId, chatId, jwtToken);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = CHAT_PATH + "/member/remove/{userId}", method = RequestMethod.POST)
    ResponseEntity removeMemberWithId(@RequestHeader("Authorization") String jwtToken,
                                      @PathVariable("chatId") UUID chatId,
                                      @PathVariable("userId") UUID userId) {
        chatService.removeMemberWithId(userId, chatId, jwtToken);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = CHAT_PATH + "/{chatId}", method = RequestMethod.POST)
    ResponseEntity deleteChatWithId(@RequestHeader("Authorization") String jwtToken,
                              @PathVariable("chatId") UUID chatId) {
        chatService.deleteChatById(chatId, jwtToken);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
