package dev.thural.quietspace.websocket.event.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@Getter
@Setter
@SuperBuilder
public class ChatEvent extends BaseEvent {

    private UUID chatId;
    private UUID actorId;
    private UUID messageId;
    private UUID recipientId;

}
