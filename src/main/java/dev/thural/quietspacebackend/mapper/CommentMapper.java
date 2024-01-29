package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.model.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    CommentEntity commentDtoToEntity(CommentDto commentDto);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    CommentDto commentEntityToDto(CommentEntity commentEntity);
}
