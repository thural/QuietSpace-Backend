package dev.thural.quietspacebackend.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    UUID userId;

    @NotNull
    UUID commentId;

    String username;

    @JsonIgnore
    private OffsetDateTime createDate;

    @JsonIgnore
    private OffsetDateTime updateDate;

}
