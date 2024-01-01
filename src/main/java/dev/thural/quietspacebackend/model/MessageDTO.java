package dev.thural.quietspacebackend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private UUID id;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID receiverId;

    @NotNull
    boolean delivered;

    @NotNull
    boolean seen;

    @NotNull
    private OffsetDateTime createDate;

    @NotNull
    private OffsetDateTime updateDate;

}
