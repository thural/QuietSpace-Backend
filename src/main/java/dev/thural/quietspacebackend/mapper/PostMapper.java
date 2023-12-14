package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.model.PostDTO;
import org.mapstruct.Mapper;

@Mapper
public interface PostMapper {
    PostEntity postDtoToEntity(PostDTO postDTO);

    PostDTO postEntityToDto(PostEntity postEntity);
}
