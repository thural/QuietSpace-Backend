package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import org.mapstruct.Mapper;

@Mapper
public interface PostLikeMapper {
    PostLikeEntity postLikeDtoToEntity(PostLikeDTO postLikeDTO);

    PostLikeDTO postLikeEntityToDto(PostLikeEntity postLikeEntity);
}