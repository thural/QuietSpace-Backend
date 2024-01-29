package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    UserEntity userDtoToEntity(UserDto userDto);

    @Mapping(target = "password", ignore = true)
    UserDto userEntityToDto(UserEntity userEntity);
}
