package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    private OffsetDateTime createDate = OffsetDateTime.now();

    @NotNull
    private OffsetDateTime updateDate = OffsetDateTime.now();

}
