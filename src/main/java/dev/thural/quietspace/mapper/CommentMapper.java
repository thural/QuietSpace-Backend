package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.model.response.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    Comment commentRequestToEntity(CommentRequest commentRequest);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    CommentResponse commentEntityToResponse(Comment comment);
}
