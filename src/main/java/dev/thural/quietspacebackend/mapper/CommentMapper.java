package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.model.CommentDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CommentMapper {
    CommentEntity commentDtoToEntity(CommentDTO commentDTO);

    CommentDTO commentEntityToDto(CommentEntity commentEntity);
}
