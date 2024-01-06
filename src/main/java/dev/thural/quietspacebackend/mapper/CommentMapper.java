package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.model.CommentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    CommentEntity commentDtoToEntity(CommentDTO commentDTO);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    CommentDTO commentEntityToDto(CommentEntity commentEntity);
}
