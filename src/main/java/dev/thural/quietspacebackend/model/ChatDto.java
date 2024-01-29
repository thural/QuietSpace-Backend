package dev.thural.quietspacebackend.model;

import jakarta.persistence.*;
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
public class ChatDto {

    private UUID id;

    @Version
    private Integer version;

    @NotNull
    private List<UserDto> users;

    private List<UUID> userIds;

    private List<MessageDto> messages;

    private OffsetDateTime createDate;

    private OffsetDateTime updateDate;

}
