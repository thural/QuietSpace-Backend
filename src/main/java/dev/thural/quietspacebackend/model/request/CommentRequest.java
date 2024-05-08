package dev.thural.quietspacebackend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID postId;

    @NotBlank
    @Size(min = 1, max = 255)
    private String text;

}
