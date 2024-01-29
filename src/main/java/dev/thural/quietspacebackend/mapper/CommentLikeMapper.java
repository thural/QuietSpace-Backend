package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.model.CommentLikeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentLikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CommentLikeEntity commentLikeDtoToEntity(CommentLikeDto commentLikeDto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CommentLikeDto commentLikeEntityToDto(CommentLikeEntity commentLikeEntity);
}