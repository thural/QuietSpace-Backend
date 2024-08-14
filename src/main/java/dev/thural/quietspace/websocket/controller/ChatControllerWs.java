package dev.thural.quietspace.websocket.controller;

import dev.thural.quietspace.model.request.MessageRequest;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.service.ChatService;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.utils.enums.EventType;
import dev.thural.quietspace.websocket.event.message.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatControllerWs {

    private final SimpMessagingTemplate template;
    private final MessageService messageService;
    private final ChatService chatService;

    public static final String CHAT_PATH = "/private/chat";
    public static final String CHAT_EVENT_PATH = CHAT_PATH + "/event";
    public static final String LEAVE_CHAT_PATH = CHAT_PATH + "/leave";
    public static final String JOIN_CHAT_PATH = CHAT_PATH + "/join";
    public static final String DELETE_MESSAGE_PATH = CHAT_PATH + "/delete/{messageId}";
    public static final String SEEN_MESSAGE_PATH = CHAT_PATH + "/seen/{messageId}";
    public static final String PUBLIC_CHAT_PATH = "/public/chat";


    @MessageMapping(PUBLIC_CHAT_PATH)
    @SendTo(PUBLIC_CHAT_PATH)
    MessageRequest sendMessageToAll(final MessageRequest message) {
        log.info("received message at {} topic: {}", PUBLIC_CHAT_PATH, message.getText());
        return message;
    }


    @MessageMapping(CHAT_PATH)
    void sendMessageToUser(@Payload MessageRequest message) {
        log.info("received message at {} topic: {}, sent by: {}", CHAT_PATH, message.getText(), message.getSenderId());
        try {
            MessageResponse savedMessage = messageService.addMessage(message);
            template.convertAndSendToUser("tommy", CHAT_PATH, savedMessage);
        } catch (Exception e) {
            var chatEvent = ChatEvent.builder()
                    .message(e.getMessage())
                    .chatId(message.getChatId())
                    .actorId(message.getSenderId())
                    .type(EventType.EXCEPTION)
                    .build();
            template.convertAndSendToUser("tommy", CHAT_EVENT_PATH, chatEvent);
        }
    }


    @MessageMapping(DELETE_MESSAGE_PATH)
    void deleteMessageById(@Payload ChatEvent chatEvent) {
        log.info("deleting message with id {} ...", chatEvent.getMessageId());
        var chatevent = ChatEvent.builder()
                .chatId(chatEvent.getChatId())
                .actorId(chatEvent.getActorId())
                .messageId(chatEvent.getMessageId())
                .type(EventType.DELETE_MESSAGE)
                .build();
        try {
            var message = messageService.deleteMessage(chatEvent.getMessageId())
                    .orElseThrow(RuntimeException::new);
            chatevent.setChatId(message.getChatId());
            template.convertAndSendToUser(message.getRecipientId().toString(), CHAT_EVENT_PATH, chatevent);
        } catch (Exception e) {
            chatevent.setMessage(e.getMessage());
            chatevent.setType(EventType.EXCEPTION);
            template.convertAndSendToUser(chatevent.getActorId().toString(), CHAT_EVENT_PATH, chatEvent);
        }
    }


    @MessageMapping(SEEN_MESSAGE_PATH)
    void markMessageSeen(@DestinationVariable UUID messageId) {
        log.info("setting message with id {} as seen ...", messageId);
        messageService.setMessageSeen(messageId)
                .ifPresent(message ->
                        template.convertAndSendToUser(
                                message.getRecipientId().toString(),
                                CHAT_EVENT_PATH,
                                ChatEvent.builder()
                                        .chatId(message.getChatId())
                                        .messageId(message.getId())
                                        .type(EventType.SEEN_MESSAGE)
                                        .build())
                );

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
            template.convertAndSendToUser(userList.get(0).getId().toString(), CHAT_PATH, chatEvent);

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
            template.convertAndSendToUser(event.getActorId().toString(), CHAT_PATH, chatEvent);

        } catch (Exception e) {
            chatEvent.setMessage(e.getMessage());
            chatEvent.setType(EventType.EXCEPTION);
            template.convertAndSendToUser(chatEvent.getActorId().toString(), CHAT_EVENT_PATH, chatEvent);
        }
    }


}
