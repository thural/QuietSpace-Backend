package dev.thural.quietspace.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse extends BaseResponse {

    private String bio;
    private String role;
    private String email;
    private String username;
    private Boolean isFollower;
    private Boolean isFollowing;
    private PhotoResponse photo;
    private Boolean isPrivateAccount;
    private ProfileSettingsResponse settings;

}