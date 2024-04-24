package dev.thural.quietspacebackend.model.response;

import dev.thural.quietspacebackend.entity.CommentLike;
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
public class CommentResponse {

    private UUID id;

    @NotNull
    UUID userId;

    @NotNull
    UUID postId;

    private String username;

    @NotNull
    @NotBlank
    private String text;

    private List<CommentLike> likes;

    private OffsetDateTime createDate = OffsetDateTime.now();
    private OffsetDateTime updateDate = OffsetDateTime.now();

}
