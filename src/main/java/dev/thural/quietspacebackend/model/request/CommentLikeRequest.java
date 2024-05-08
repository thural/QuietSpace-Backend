package dev.thural.quietspacebackend.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID commentId;

}
