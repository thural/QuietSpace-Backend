package dev.thural.quietspace.controller;

import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.MessageRepository;
import dev.thural.quietspace.service.ChatService;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.utils.enums.EventType;
import dev.thural.quietspace.websocket.event.message.ChatEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate template;
    private final MessageService messageService;

    public static final String CHAT_PATH = "/api/v1/chats";
    public static final String SOCKET_CHAT_PATH = "/private/chat";
    public static final String CHAT_EVENT_PATH = SOCKET_CHAT_PATH + "/event";
    public static final String LEAVE_CHAT_PATH = SOCKET_CHAT_PATH + "/leave";
    public static final String JOIN_CHAT_PATH = SOCKET_CHAT_PATH + "/join";
    public static final String DELETE_MESSAGE_PATH = SOCKET_CHAT_PATH + "/delete/{messageId}";
    public static final String SEEN_MESSAGE_PATH = SOCKET_CHAT_PATH + "/seen/{messageId}";
    public static final String PUBLIC_CHAT_PATH = "/public/chat";

    @GetMapping("/{chatId}")
    ResponseEntity<ChatResponse> getSingleChatById(@PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }

    @GetMapping("/members/{userId}")
    ResponseEntity<List<ChatResponse>> getChatsByMemberId(@PathVariable UUID userId) {
        return ResponseEntity.ok(chatService.getChatsByUserId(userId));
    }

    @PostMapping
    ResponseEntity<ChatResponse> createChat(@RequestBody ChatRequest chat) {
        //TODO: update chat members over socket
        return ResponseEntity.ok(chatService.createChat(chat));
    }

    @PatchMapping("/{chatId}/members/add/{userId}")
    ResponseEntity<UserResponse> addMemberWithId(@PathVariable UUID userId, @PathVariable UUID chatId) {
        //TODO: update chat members over socket
        return ResponseEntity.ok(chatService.addMemberWithId(userId, chatId));
    }

    @PatchMapping("/{chatId}/members/remove/{userId}")
    ResponseEntity<?> removeMemberWithId(@PathVariable UUID chatId, @PathVariable UUID userId) {
        //TODO: update chat members over socket
        chatService.removeMemberWithId(userId, chatId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{chatId}")
    ResponseEntity<?> deleteChatWithId(@PathVariable("chatId") UUID chatId) {
        //TODO: update chat members over socket
        chatService.deleteChatById(chatId);
        return ResponseEntity.noContent().build();
    }

    @MessageMapping(PUBLIC_CHAT_PATH)
    @SendTo(PUBLIC_CHAT_PATH)
    MessageRequest sendMessageToAll(final MessageRequest message) {
        log.info("received message at {} topic: {}", PUBLIC_CHAT_PATH, message.getText());
        return message;
    }


    @MessageMapping(SOCKET_CHAT_PATH)
    void sendMessageToUser(@Payload MessageRequest message) {
        log.info("received message at {} topic: {}, sent by: {}", SOCKET_CHAT_PATH, message.getText(), message.getSenderId());
        try {
            MessageResponse savedMessage = messageService.addMessage(message);
            template.convertAndSendToUser(savedMessage.getRecipientId().toString(), SOCKET_CHAT_PATH, savedMessage);
            template.convertAndSendToUser(savedMessage.getSenderId().toString(), SOCKET_CHAT_PATH, savedMessage);
        } catch (Exception e) {
            var chatEvent = ChatEvent.builder()
                    .message(e.getMessage())
                    .chatId(message.getChatId())
                    .actorId(message.getSenderId())
                    .type(EventType.EXCEPTION)
                    .build();
            template.convertAndSendToUser(message.getRecipientId().toString(), CHAT_EVENT_PATH, chatEvent);
        }
    }


    @MessageMapping(DELETE_MESSAGE_PATH)
    void deleteMessageById(@DestinationVariable UUID messageId) {
        log.info("deleting message with id {} ...", messageId);

        Message foundMessage = messageRepository.findById(messageId)
                .orElseThrow(EntityNotFoundException::new);

        var chatevent = ChatEvent.builder()
                .chatId(foundMessage.getChat().getId())
                .actorId(foundMessage.getSender().getId())
                .messageId(foundMessage.getId())
                .type(EventType.DELETE_MESSAGE)
                .build();
        try {
            MessageResponse message = messageService.deleteMessage(messageId)
                    .orElseThrow(RuntimeException::new);
            chatevent.setChatId(message.getChatId());

            template.convertAndSendToUser(message.getRecipientId().toString(), CHAT_EVENT_PATH, chatevent);
            template.convertAndSendToUser(message.getSenderId().toString(), CHAT_EVENT_PATH, chatevent);
        } catch (Exception e) {
            chatevent.setMessage(e.getMessage());
            chatevent.setType(EventType.EXCEPTION);

            template.convertAndSendToUser(chatevent.getActorId().toString(), CHAT_EVENT_PATH, chatevent);
        }
    }


    @MessageMapping(SEEN_MESSAGE_PATH)
    void markMessageSeen(@DestinationVariable UUID messageId) {
        log.info("setting message with id {} as seen ...", messageId);

        MessageResponse message = messageService.setMessageSeen(messageId)
                .orElseThrow(EntityNotFoundException::new);

        var chatEvent = ChatEvent.builder()
                .chatId(message.getChatId())
                .messageId(message.getId())
                .type(EventType.SEEN_MESSAGE)
                .build();

        template.convertAndSendToUser(message.getSenderId().toString(), CHAT_EVENT_PATH, chatEvent);
        template.convertAndSendToUser(message.getRecipientId().toString(), CHAT_EVENT_PATH, chatEvent);
    }


    @MessageMapping(LEAVE_CHAT_PATH)
    void processLeftChat(@Payload ChatEvent event) {
        log.info("user {} is leaving chat {} ...", event.getActorId(), event.getChatId());
        var chatEvent = ChatEvent.builder()
                .message("user has left the chat")
                .chatId(event.getChatId())
                .actorId(event.getActorId())
                .type(EventType.LEFT_CHAT)
                .build();
        try {
            // TODO: send to all chat members
            var userList = chatService.removeMemberWithId(event.getActorId(), event.getChatId());
            template.convertAndSendToUser(userList.get(0).getId().toString(), SOCKET_CHAT_PATH, chatEvent);

        } catch (Exception e) {
            chatEvent.setMessage(e.getMessage());
            chatEvent.setType(EventType.EXCEPTION);
            template.convertAndSendToUser(chatEvent.getActorId().toString(), CHAT_EVENT_PATH, chatEvent);
        }
    }


    @MessageMapping(JOIN_CHAT_PATH)
    void processJoinChat(@Payload ChatEvent event) {
        log.info("user {} is being added to chat {} ...", event.getRecipientId(), event.getChatId());
        var chatEvent = ChatEvent.builder()
                .chatId(event.getChatId())
                .actorId(event.getActorId())
                .type(EventType.JOINED_CHAT)
                .build();
        try {
            chatService.addMemberWithId(event.getRecipientId(), event.getChatId());

            chatEvent.setMessage(String.format(
                    "user %s has been added to chat %s ...",
                    event.getRecipientId(),
                    event.getChatId()
            ));
            // TODO: send to all chat members
            template.convertAndSendToUser(event.getActorId().toString(), SOCKET_CHAT_PATH, chatEvent);

        } catch (Exception e) {
            chatEvent.setMessage(e.getMessage());
            chatEvent.setType(EventType.EXCEPTION);
            template.convertAndSendToUser(chatEvent.getActorId().toString(), CHAT_EVENT_PATH, chatEvent);
        }
    }

}
