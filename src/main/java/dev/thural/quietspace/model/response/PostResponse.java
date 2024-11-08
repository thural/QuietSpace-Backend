package dev.thural.quietspace.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private UUID id;
    private UUID userId;
    private UUID repostId;
    private String username;
    private String title;
    private String text;
    private String repostText;
    private PollResponse poll;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private ReactionResponse userReaction;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}