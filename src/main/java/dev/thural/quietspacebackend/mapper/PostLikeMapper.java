package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.model.PostLikeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostLikeMapper {
    @Mapping(target = "id", ignore = true)
    PostLikeEntity postLikeDtoToEntity(PostLikeDto postLikeDto);

    PostLikeDto postLikeEntityToDto(PostLikeEntity postLikeEntity);
}