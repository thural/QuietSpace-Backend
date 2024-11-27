package dev.thural.quietspace.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileSettings extends BaseEntity {

    public ProfileSettings(User user) {
        this.user = user;
    }

    @NotNull
    @OneToOne
    User user;

    String bio;

    @NotNull
    @Builder.Default
    List<User> blockedUsers = new ArrayList<>();

    @Builder.Default
    Boolean isPrivateAccount = false;
    @Builder.Default
    Boolean isNotificationsMuted = false;
    @Builder.Default
    Boolean isAllowPublicGroupChatInvite = true;
    @Builder.Default
    Boolean isAllowPublicMessageRequests = true;
    @Builder.Default
    Boolean isAllowPublicComments = true;
    @Builder.Default
    Boolean isHideLikeCounts = false;

}
