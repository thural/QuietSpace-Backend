package dev.thural.quietspacebackend.model.response;

import dev.thural.quietspacebackend.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowResponse {

    @NotNull
    private User followingId;

    @NotNull
    private User followerId;

}
