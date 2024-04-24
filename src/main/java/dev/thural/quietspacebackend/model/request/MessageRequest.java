package dev.thural.quietspacebackend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    private UUID chatId;
    @NotNull
    private UUID senderId;

}
