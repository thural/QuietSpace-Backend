package dev.thural.quietspacebackend.model.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String role;
    private String username;
    private String email;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;

}