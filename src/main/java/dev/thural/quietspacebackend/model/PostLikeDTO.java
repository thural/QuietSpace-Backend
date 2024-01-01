package dev.thural.quietspacebackend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDTO {
    private UUID id;

    @NotNull
    @NotBlank
    String username;

    @NotNull
    UUID userId;

    @NotNull
    UUID postId;

    @NotNull
    private OffsetDateTime createDate;

    @NotNull
    private OffsetDateTime updateDate;
}
