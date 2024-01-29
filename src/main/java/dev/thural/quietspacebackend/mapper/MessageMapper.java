package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.model.MessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {
    @Mapping(target = "id", ignore = true)
    MessageEntity messageDtoToEntity(MessageDto messageDto);

    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "username", source = "sender.username")
    MessageDto messageEntityToDto(MessageEntity messageEntity);
}
