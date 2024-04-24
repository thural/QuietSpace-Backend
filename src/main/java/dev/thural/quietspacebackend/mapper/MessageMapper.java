package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.Message;
import dev.thural.quietspacebackend.model.request.MessageRequest;
import dev.thural.quietspacebackend.model.response.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {
    @Mapping(target = "id", ignore = true)
    Message messageRequestToEntity(MessageRequest messageRequest);

    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "username", source = "sender.username")
    MessageResponse messageEntityToDto(Message messageEntity);

}
