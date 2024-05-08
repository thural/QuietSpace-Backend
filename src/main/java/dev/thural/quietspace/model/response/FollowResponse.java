package dev.thural.quietspace.model.response;

import dev.thural.quietspace.entity.User;
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
