package dev.thural.quietspace.model.response;

import lombok.*;

import java.time.OffsetDateTime;
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
    private OffsetDateTime createDate = OffsetDateTime.now();
    private OffsetDateTime updateDate = OffsetDateTime.now();
    // TODO: include replied comment id
    // TODO: include like counts
    // TODO: include dislike counts

}
