package dev.thural.quietspacebackend.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeResponse {

    private UUID id;
    private UUID userId;
    private String username;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}
