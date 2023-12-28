package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.model.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    PostEntity postDtoToEntity(PostDTO postDTO);

    PostDTO postEntityToDto(PostEntity postEntity);
}
