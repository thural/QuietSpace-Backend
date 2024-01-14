package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
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
public class ChatDTO {

    private UUID id;

    @Version
    private Integer version;

    @NotNull
    private List<UserEntity> users;

    private List<MessageEntity> messages;

    private OffsetDateTime createDate;

    private OffsetDateTime updateDate;

}
