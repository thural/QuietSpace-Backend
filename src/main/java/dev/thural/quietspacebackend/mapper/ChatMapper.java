package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.model.ChatDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChatMapper {
    @Mapping(target = "id", ignore = true)
    ChatEntity chatDtoToEntity(ChatDTO chatDTO);

    ChatDTO chatEntityToDto(ChatEntity chatEntity);
}