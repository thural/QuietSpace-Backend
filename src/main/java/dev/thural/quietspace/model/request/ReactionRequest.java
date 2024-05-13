package dev.thural.quietspace.model.request;

import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.LikeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID contentId;

    @NotNull
    private ContentType contentType;

    @NotNull
    private LikeType likeType;

}