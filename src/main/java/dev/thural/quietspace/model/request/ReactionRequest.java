package dev.thural.quietspace.model.request;

import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {

    @NotNull(message = "reaction user id can not be null")
    private UUID userId;

    @NotNull(message = "reaction content id can not be null")
    private UUID contentId;

    @NotNull(message = "reaction content type can not be null")
    private ContentType contentType;

    @NotNull(message = "reaction type can not be null")
    private ReactionType reactionType;

}