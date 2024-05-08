package dev.thural.quietspace.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeResponse {

    private UUID id;
    private UUID userId;
    private UUID commentId;
    private String username;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}
