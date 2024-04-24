package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.request.MessageRequest;
import dev.thural.quietspacebackend.model.response.MessageResponse;
import dev.thural.quietspacebackend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MessageController {

    public static final String MESSAGE_PATH = "/api/v1/messages";
    public static final String MESSAGE_PATH_ID = MESSAGE_PATH + "/{messageId}";

    private final MessageService messageService;


    @RequestMapping(value = MESSAGE_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createMessage(@RequestBody @Validated MessageRequest messageRequest) {
        MessageResponse savedMessage = messageService.addMessage(messageRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", MESSAGE_PATH + "/" + savedMessage.getId());
        return new ResponseEntity<>(savedMessage, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = MESSAGE_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deleteMessage(@PathVariable("messageId") UUID messageId) {
        messageService.deleteMessage(messageId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = MESSAGE_PATH + "/chat/{chatId}", method = RequestMethod.GET)
    Page<MessageResponse> getMessagesByChatId(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize,
            @PathVariable("chatId") UUID chatId) {
        return messageService.getMessagesByChatId(pageNumber, pageSize, chatId);
    }

}
