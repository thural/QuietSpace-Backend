package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.Follow;
import dev.thural.quietspacebackend.model.response.FollowResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FollowMapper {
    @Mapping(target = "id", ignore = true)
    Follow followResponseToEntity(FollowResponse followResponse);

    FollowResponse followEntityToResponse(Follow followEntity);
}
