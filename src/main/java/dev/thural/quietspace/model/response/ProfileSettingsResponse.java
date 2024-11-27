package dev.thural.quietspace.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileSettingsResponse extends BaseResponse {

    String bio;

    @NotNull
    List<UUID> blockedUserIds;

    Boolean isPrivateAccount;
    Boolean isNotificationsMuted;
    Boolean isAllowPublicGroupChatInvite;
    Boolean isAllowPublicMessageRequests;
    Boolean isAllowPublicComments;
    Boolean isHideLikeCounts;

}