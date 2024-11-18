package dev.thural.quietspace.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSettingsRequest {

    String bio;

    Boolean isPrivateAccount = false;
    Boolean isNotificationsMuted = false;
    Boolean isAllowPublicGroupChatInvite = true;
    Boolean isAllowPublicMessageRequests = true;
    Boolean isAllowPublicComments = true;
    Boolean isHideLikeCounts = false;

}