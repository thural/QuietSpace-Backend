package dev.thural.quietspace.model.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {

    private UUID followingId;
    private UUID followerId;

}
