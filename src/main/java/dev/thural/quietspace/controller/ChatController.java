package dev.thural.quietspace.controller;

import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.model.request.CreateChatRequest;
import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.MessageRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.ChatService;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.websocket.event.message.ChatEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static dev.thural.quietspace.shared.enums.EventType.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public static final String CHAT_PATH = "/api/v1/chats";
    public static final String CHAT_REMOVE_MEMBER_PATH = "/{chatId}/members/remove/{userId}";
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
    ResponseEntity<ChatResponse> createChat(@RequestBody CreateChatRequest chat) {
        //TODO: update chat members over socket
        return ResponseEntity.ok(chatService.createChat(chat));
    }

    @PatchMapping("/{chatId}/members/add/{userId}")
    ResponseEntity<UserResponse> addMemberWithId(@PathVariable UUID userId, @PathVariable UUID chatId) {
        //TODO: update chat members over socket
        return ResponseEntity.ok(chatService.addMemberWithId(userId, chatId));
    }

    @PatchMapping(CHAT_REMOVE_MEMBER_PATH)
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
        log.warn("CHAT CONTROLLER: received message at {} topic: {}", PUBLIC_CHAT_PATH, message.getText());
        return message;
    }


    @MessageMapping(SOCKET_CHAT_PATH)
    @SendTo(PUBLIC_CHAT_PATH)
    MessageResponse sendMessageToUser(MessageRequest message) {
        log.warn("received message at {} topic: {}, sent by: {}", SOCKET_CHAT_PATH, message.getText(), message.getSenderId());
        if (message.getSenderId() != null) {
            userRepository.findById(message.getSenderId()).ifPresent(user -> {
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        return messageService.addMessage(message);
    }


    @MessageMapping(DELETE_MESSAGE_PATH)
    @SendTo(CHAT_EVENT_PATH)
    ChatEvent deleteMessageById(@DestinationVariable UUID messageId) {
        log.info("deleting message with id {} ...", messageId);
        Message foundMessage = messageRepository.findById(messageId).orElseThrow(EntityNotFoundException::new);
        if (foundMessage.getSender() != null) {
            var user = foundMessage.getSender();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        }
        var chatevent = ChatEvent.builder()
                .chatId(foundMessage.getChat().getId())
                .actorId(foundMessage.getSender().getId())
                .messageId(foundMessage.getId())
                .type(DELETE_MESSAGE)
                .build();
        try {
            MessageResponse message = messageService.deleteMessage(messageId)
                    .orElseThrow(RuntimeException::new);
            chatevent.setChatId(message.getChatId());
        } catch (Exception e) {
            chatevent.setMessage(e.getMessage());
            chatevent.setType(EXCEPTION);
        }
        return chatevent;
    }


    @MessageMapping(SEEN_MESSAGE_PATH)
    @SendTo(CHAT_EVENT_PATH)
    ChatEvent markMessageSeen(@DestinationVariable UUID messageId) {
        log.info("setting message with id {} as seen ...", messageId);
        MessageResponse message = messageService.setMessageSeen(messageId).orElseThrow(EntityNotFoundException::new);
        return ChatEvent.builder()
                .chatId(message.getChatId())
                .messageId(message.getId())
                .type(SEEN_MESSAGE)
                .build();
    }


    @MessageMapping(LEAVE_CHAT_PATH)
    @SendTo(PUBLIC_CHAT_PATH)
    ChatEvent processLeftChat(ChatEvent event) {
        log.warn("user {} is leaving chat {} ...", event.getActorId(), event.getChatId());
        if (event.getActorId() != null) {
            userRepository.findById(event.getActorId()).ifPresent(user -> {
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        var chatEvent = ChatEvent.builder()
                .message("user has left the chat")
                .chatId(event.getChatId())
                .actorId(event.getActorId())
                .type(LEFT_CHAT)
                .build();
        try {
            chatService.removeMemberWithId(event.getActorId(), event.getChatId());
        } catch (Exception e) {
            chatEvent.setMessage(e.getMessage());
            chatEvent.setType(EXCEPTION);
            log.warn("Exception in processLeftChat: {}", e.getMessage(), e);
        }
        log.warn("processLeftChat returning chatEvent type={} chatId={}", chatEvent.getType(), chatEvent.getChatId());
        return chatEvent;
    }


    @MessageMapping(JOIN_CHAT_PATH)
    @SendTo(PUBLIC_CHAT_PATH)
    ChatEvent processJoinChat(@Payload ChatEvent event) {
        log.info("user {} is being added to chat {} ...", event.getRecipientId(), event.getChatId());
        if (event.getActorId() != null) {
            userRepository.findById(event.getActorId()).ifPresent(user -> {
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        var chatEvent = ChatEvent.builder()
                .chatId(event.getChatId())
                .actorId(event.getActorId())
                .type(JOINED_CHAT)
                .build();
        try {
            chatService.addMemberWithId(event.getRecipientId(), event.getChatId());

            chatEvent.setMessage(String.format(
                    "user %s has been added to chat %s ...",
                    event.getRecipientId(),
                    event.getChatId()
            ));
        } catch (Exception e) {
            chatEvent.setMessage(e.getMessage());
            chatEvent.setType(EXCEPTION);
        }
        return chatEvent;
    }

}
