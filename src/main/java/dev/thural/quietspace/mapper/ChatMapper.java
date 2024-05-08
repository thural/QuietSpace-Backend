package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChatMapper {

    @Mapping(target = "id", ignore = true)
    Chat chatRequestToEntity(ChatRequest chatRequest);

    ChatResponse chatEntityToResponse(Chat chat);
}