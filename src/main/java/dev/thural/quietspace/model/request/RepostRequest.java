package dev.thural.quietspace.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepostRequest {
    @NotBlank
    private String text;
    @NotNull
    private String postId;
}
