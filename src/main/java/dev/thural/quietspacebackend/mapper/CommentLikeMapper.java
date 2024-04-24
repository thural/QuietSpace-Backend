package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentLike;
import dev.thural.quietspacebackend.model.response.CommentLikeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentLikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CommentLike commentLikeDtoToEntity(CommentLikeResponse commentLikeResponse);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CommentLikeResponse commentLikeEntityToDto(CommentLike commentLike);
}