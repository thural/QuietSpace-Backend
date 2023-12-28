package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    UserEntity userDtoToEntity(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    UserDTO userEntityToDto(UserEntity userEntity);
}
