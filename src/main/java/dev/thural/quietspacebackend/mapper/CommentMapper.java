package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.Comment;
import dev.thural.quietspacebackend.model.request.CommentRequest;
import dev.thural.quietspacebackend.model.response.CommentResponse;
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
