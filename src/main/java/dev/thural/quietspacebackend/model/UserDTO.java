package dev.thural.quietspacebackend.model;

import dev.thural.quietspacebackend.entity.*;
import jakarta.persistence.Column;
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
public class UserDTO {

    private UUID id;

    private Integer version;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 16)
    private String role;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    private String username;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    private String email;

    @NotNull
    @NotBlank
    private String password;

    private OffsetDateTime createDate;

    private OffsetDateTime updateDate;

}