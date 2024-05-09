package dev.thural.quietspace.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private UUID id;
    private UUID userId;
    private String username;
    private String textContent;
    private List<PostLikeResponse> likes;
    private List<CommentResponse> comments;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}