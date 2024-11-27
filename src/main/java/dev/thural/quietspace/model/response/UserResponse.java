package dev.thural.quietspace.model.response;

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
    private String bio;
    private String role;
    private String email;
    private String username;
    private Boolean isFollower;
    private Boolean isFollowing;
    private PhotoResponse photo;
    private Boolean isPrivateAccount;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;
    private ProfileSettingsResponse settings;

}