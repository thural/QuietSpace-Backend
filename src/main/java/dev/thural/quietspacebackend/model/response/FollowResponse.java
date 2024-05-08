package dev.thural.quietspacebackend.model.response;

import dev.thural.quietspacebackend.entity.User;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {

    private User followingId;
    private User followerId;

}
