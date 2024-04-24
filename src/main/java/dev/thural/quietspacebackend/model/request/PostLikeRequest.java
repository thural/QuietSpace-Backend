package dev.thural.quietspacebackend.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeRequest {
    private UUID id;

    String username;

    @NotNull
    UUID userId;

    @NotNull
    UUID postId;

    private OffsetDateTime createDate;

    private OffsetDateTime updateDate;
}
