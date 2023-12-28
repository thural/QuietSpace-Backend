package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.model.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    PostEntity postDtoToEntity(PostDTO postDTO);

    @Mapping(target = ".", source ="user")
    @Mapping(target = "userId", source ="user.id")
    PostDTO postEntityToDto(PostEntity postEntity);
}
