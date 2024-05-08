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
    private UUID senderId;
    private String username;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}