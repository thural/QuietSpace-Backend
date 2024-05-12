package dev.thural.quietspace.model.request;

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
public class PostRequest {

    @NotNull
    private UUID userId;

    private String title;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;

    private PollRequest poll;

}