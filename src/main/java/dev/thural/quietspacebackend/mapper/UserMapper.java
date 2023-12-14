package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.UserDTO;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserEntity userDtoToEntity(UserDTO userDTO);

    UserDTO userEntityToDto(UserEntity userEntity);
}
