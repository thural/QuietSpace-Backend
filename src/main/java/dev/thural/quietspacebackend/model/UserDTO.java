package dev.thural.quietspacebackend.model;

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

    private OffsetDateTime date = OffsetDateTime.now();

    private String username;

    private String password;

    private List<UserDTO> friendIds;

}