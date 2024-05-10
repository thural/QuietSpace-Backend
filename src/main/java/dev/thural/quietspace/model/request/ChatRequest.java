package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    @NotNull
    private List<UUID> userIds;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 1000)
    private String message;

}
