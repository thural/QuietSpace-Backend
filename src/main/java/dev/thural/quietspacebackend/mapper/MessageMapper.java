package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.model.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {
    @Mapping(target = "id", ignore = true)
    MessageEntity messageDtoToEntity(MessageDTO messageDTO);

    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "username", source = "sender.username")
    MessageDTO messageEntityToDto(MessageEntity messageEntity);
}
