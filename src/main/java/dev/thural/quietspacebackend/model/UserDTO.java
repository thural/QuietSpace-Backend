package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.UserEntity;
import jakarta.persistence.Column;
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
public class UserDTO {

    private UUID id;

    private Integer version;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String password;

    private List<UserEntity> friendIds;

    private OffsetDateTime createDate;

    private OffsetDateTime updateDate;

}