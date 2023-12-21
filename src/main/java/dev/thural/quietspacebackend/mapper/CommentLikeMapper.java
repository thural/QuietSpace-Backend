package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CommentLikeMapper {
    CommentLikeEntity commentLikeDtoToEntity(CommentLikeDTO commentLikeDTO);

    CommentLikeDTO commentLikeEntityToDto(CommentLikeEntity commentLikeEntity);
}