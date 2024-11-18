package dev.thural.quietspace.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSettingsResponse {

    String bio;

    @NotNull
    List<UUID> blockedUserIds;

    Boolean isPrivateAccount = false;
    Boolean isNotificationsMuted = false;
    Boolean isAllowPublicGroupChatInvite = true;
    Boolean isAllowPublicMessageRequests = true;
    Boolean isAllowPublicComments = true;
    Boolean isHideLikeCounts = false;

}