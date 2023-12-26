package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.catalina.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private UUID id;

    private Integer version;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    private UserEntity user;

    @NotNull
    private OffsetDateTime createDate = OffsetDateTime.now();

    @NotNull
    private OffsetDateTime updateDate = OffsetDateTime.now();

}
