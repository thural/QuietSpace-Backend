package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDTO {

    private UUID id;

    private Integer version;

    @NotNull
    @NotBlank
    String username;

    @NotNull
    UUID userId;

    @NotNull
    UUID postId;

    @NotNull
    private OffsetDateTime createDate;

    @NotNull
    private OffsetDateTime updateDate;

}
