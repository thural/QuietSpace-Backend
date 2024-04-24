package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostLike;
import dev.thural.quietspacebackend.model.response.PostLikeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostLikeMapper {
    @Mapping(target = "id", ignore = true)
    PostLike postLikeDtoToEntity(PostLikeResponse postLikeResponse);

    PostLikeResponse postLikeEntityToDto(PostLike postLike);
}