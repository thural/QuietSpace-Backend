package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.CommentLike;
import dev.thural.quietspace.model.response.CommentLikeResponse;
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