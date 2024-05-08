package dev.thural.quietspacebackend.model.response;

import dev.thural.quietspacebackend.entity.CommentLike;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID userId;
    private UUID postId;
    private String text;
    private String username;
    private List<CommentLike> likes;
    private OffsetDateTime createDate = OffsetDateTime.now();
    private OffsetDateTime updateDate = OffsetDateTime.now();

}
