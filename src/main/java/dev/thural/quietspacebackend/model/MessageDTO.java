package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.ChatEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
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
    private UUID chatId;

    @NotNull
    private UUID senderId;

    List<UUID> deliveredTo;

    List<UUID> seenBy;

    private OffsetDateTime createDate;

    private OffsetDateTime updateDate;

}
