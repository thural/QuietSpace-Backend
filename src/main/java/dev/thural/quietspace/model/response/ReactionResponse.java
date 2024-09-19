package dev.thural.quietspace.model.response;

import dev.thural.quietspace.enums.ReactionType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {

    private UUID id;
    private UUID userId;
    private UUID contentId;
    private String username;
    private ReactionType reactionType;
    private OffsetDateTime updateDate;

}
