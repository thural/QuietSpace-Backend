package dev.thural.quietspace.model.request;

import dev.thural.quietspace.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowRequest {

    @NotNull
    private User followingId;

    @NotNull
    private User followerId;

}
