package dev.thural.quietspace.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSettingsRequest {

    String bio;

    Boolean isPrivateAccount;
    Boolean isNotificationsMuted;
    Boolean isAllowPublicGroupChatInvite;
    Boolean isAllowPublicMessageRequests;
    Boolean isAllowPublicComments;
    Boolean isHideLikeCounts;

}