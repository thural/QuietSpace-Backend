package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowDTO {

    private UUID id;

    @Version
    private Integer version;

    @NotNull
    private UserEntity following;

    @NotNull
    private UserEntity follower;

    @NotNull
    private OffsetDateTime createDate;

    @NotNull
    private OffsetDateTime updateDate;

}
