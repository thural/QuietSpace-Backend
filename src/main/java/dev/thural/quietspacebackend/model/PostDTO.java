package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    private UUID id;

    private Integer version;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    String username;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 255)
    private String text;

    @Size(max = 36)
    UUID userId;

    @NotNull
    private OffsetDateTime createDate;

    @NotNull
    private OffsetDateTime updateDate;

}
