package dev.thural.quietspace.chat.controller;

import dev.thural.quietspace.chat.ChatService;
import dev.thural.quietspace.message.Message;
import dev.thural.quietspace.message.MessageRepository;
import dev.thural.quietspace.message.MessageService;
import dev.thural.quietspace.message.dto.MessageRequest;
import dev.thural.quietspace.message.dto.MessageResponse;
import dev.thural.quietspace.user.UserRepository;
import dev.thural.quietspace.websocket.event.message.ChatEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

import static dev.thural.quietspace.shared.enums.EventType.*;
import static dev.thural.quietspace.websocket.constant.WebSocketPaths.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @MessageMapping(PUBLIC_CHAT)
    @SendTo(PUBLIC_BROKER + "/chat")
    MessageRequest sendMessageToAll(MessageRequest message) {
        log.warn("CHAT WEBSOCKET: received message at {} topic: {}", PUBLIC_CHAT, message.getText());
        return message;
    }

    @MessageMapping(PRIVATE_CHAT)
    @SendTo(PUBLIC_CHAT)
    MessageResponse sendMessageToUser(MessageRequest message) {
        log.warn("received message at {} topic: {}, sent by: {}", PRIVATE_CHAT, message.getText(), message.getSenderId());
        return messageService.addMessage(message);
    }

    @MessageMapping(DELETE_MESSAGE)
    @SendTo(CHAT_EVENT)
    ChatEvent deleteMessageById(@DestinationVariable UUID messageId) {
        log.info("deleting message with id {} ...", messageId);
        Message foundMessage = messageRepository.findById(messageId).orElseThrow(EntityNotFoundException::new);
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

    @MessageMapping(SEEN_MESSAGE)
    @SendTo(CHAT_EVENT)
    ChatEvent markMessageSeen(@DestinationVariable UUID messageId) {
        log.info("setting message with id {} as seen ...", messageId);
        MessageResponse message = messageService.setMessageSeen(messageId).orElseThrow(EntityNotFoundException::new);
        return ChatEvent.builder()
                .chatId(message.getChatId())
                .messageId(message.getId())
                .type(SEEN_MESSAGE)
                .build();
    }

    @MessageMapping(LEAVE_CHAT)
    @SendTo(PUBLIC_CHAT)
    ChatEvent processLeftChat(ChatEvent event) {
        log.warn("user {} is leaving chat {} ...", event.getActorId(), event.getChatId());
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

    @MessageMapping(JOIN_CHAT)
    @SendTo(PUBLIC_CHAT)
    ChatEvent processJoinChat(@Payload ChatEvent event) {
        log.info("user {} is being added to chat {} ...", event.getRecipientId(), event.getChatId());
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
