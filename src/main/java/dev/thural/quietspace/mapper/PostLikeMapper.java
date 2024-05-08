package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.PostLike;
import dev.thural.quietspace.model.response.PostLikeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostLikeMapper {
    @Mapping(target = "id", ignore = true)
    PostLike postLikeDtoToEntity(PostLikeResponse postLikeResponse);

    PostLikeResponse postLikeEntityToDto(PostLike postLike);
}