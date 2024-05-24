package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Follow;
import dev.thural.quietspace.model.response.FollowResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FollowMapper {
    @Mapping(target = "id", ignore = true)
    Follow followResponseToEntity(FollowResponse followResponse);

    @Mapping(target = "followingId", source = "following.id")
    @Mapping(target = "followerId", source = "follower.id")
    FollowResponse followEntityToResponse(Follow followEntity);
}
