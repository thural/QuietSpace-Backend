package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.model.PostDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    PostEntity postDtoToEntity(PostDto postDto);

    @Mapping(target = "username", source ="user.username")
    @Mapping(target = "userId", source ="user.id")
    PostDto postEntityToDto(PostEntity postEntity);
}
