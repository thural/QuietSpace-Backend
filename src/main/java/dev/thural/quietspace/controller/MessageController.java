package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
public class MessageController {

    public static final String MESSAGE_PATH = "/api/v1/messages";
    public static final String MESSAGE_PATH_ID = "/{messageId}";

    private final MessageService messageService;


    @PostMapping
    ResponseEntity<MessageResponse> createMessage(@ModelAttribute @Validated MessageRequest messageRequest) {
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

    @GetMapping("/chat/{chatId}")
    ResponseEntity<MessageResponse> getMessagesById(@PathVariable UUID messageId, @PathVariable UUID chatId) {
        return ResponseEntity.ok(messageService.getMessageById(messageId, chatId));
    }

}
