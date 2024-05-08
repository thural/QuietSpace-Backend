package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID postId;

}
