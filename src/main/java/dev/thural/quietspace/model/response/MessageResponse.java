package dev.thural.quietspace.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID id;
    private UUID chatId;
    private String text;
    private Boolean isSeen;
    private UUID senderId;
    private UUID recipientId;
    private String senderName;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}