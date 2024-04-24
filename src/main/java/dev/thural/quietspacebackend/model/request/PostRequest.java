package dev.thural.quietspacebackend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    private UUID id;
    private UUID userId;
    private String username;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 255)
    private String textContent;

    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}