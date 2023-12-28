package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentLikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CommentLikeEntity commentLikeDtoToEntity(CommentLikeDTO commentLikeDTO);

    CommentLikeDTO commentLikeEntityToDto(CommentLikeEntity commentLikeEntity);
}