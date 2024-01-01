package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private UUID id;

    @NotNull
    UUID userId;

    @NotNull
    UUID postId;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String text;

    private List<CommentLikeEntity> likes;

    @NotNull
    private OffsetDateTime createDate = OffsetDateTime.now();

    @NotNull
    private OffsetDateTime updateDate = OffsetDateTime.now();

}
