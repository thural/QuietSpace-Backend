package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.PostLike;
import dev.thural.quietspace.model.response.PostLikeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostLikeMapper {
    @Mapping(target = "id", ignore = true)
    PostLike postLikeDtoToEntity(PostLikeResponse postLikeResponse);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    PostLikeResponse postLikeEntityToResponse(PostLike postLike);
}