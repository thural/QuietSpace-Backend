package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.FollowEntity;
import dev.thural.quietspacebackend.model.FollowDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FollowMapper {
    @Mapping(target = "id", ignore = true)
    FollowEntity followDtoToEntity(FollowDTO followDTO);

    FollowDTO followEntityToDto(FollowEntity followEntity);
}
