package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
public class MessageController {

    public static final String MESSAGE_PATH = "/api/v1/messages";
    public static final String MESSAGE_PATH_ID = "/{messageId}";

    private final MessageService messageService;
    private final ReactionService reactionService;


    @PostMapping
    ResponseEntity<MessageResponse> createMessage(@RequestBody @Validated MessageRequest messageRequest) {
        return ResponseEntity.ok(messageService.addMessage(messageRequest));
    }

    @DeleteMapping(MESSAGE_PATH_ID)
    ResponseEntity<?> deleteMessage(@PathVariable UUID messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chat/{chatId}")
    Page<MessageResponse> getMessagesByChatId(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize,
            @PathVariable UUID chatId
    ) {
        return messageService.getMessagesByChatId(pageNumber, pageSize, chatId);
    }

    @PostMapping("/toggle-reaction")
    ResponseEntity<?> toggleMessageLike(ReactionRequest reaction) {
        reactionService.handleReaction(reaction);
        return ResponseEntity.ok().build();
    }

}
