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

    @NotNull(message = "user id can not be null")
    private UUID userId;

    private String title;

    @NotNull(message = "text content can not be null")
    @NotBlank(message = "post text can not be null")
    @Size(min = 1, max = 1000, message = "at least 1 and max 1000 characters expected")
    private String text;

    private PollRequest poll;

}