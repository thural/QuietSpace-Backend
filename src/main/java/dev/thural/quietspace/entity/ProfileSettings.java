package dev.thural.quietspace.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @NotNull
    Boolean isPrivateAccount = false;

    String bio;

    @NotNull
    Boolean isNotificationsMuted = false;

    @NotNull
    List<User> blockedUsers = new ArrayList<>();

    @NotNull
    Boolean isAllowPublicGroupChatInvite = true;
    Boolean isAllowPublicMessageRequests = true;
    Boolean isAllowPublicComments = true;
    Boolean isHideLikeCounts = false;

}
