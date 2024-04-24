package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.Chat;
import dev.thural.quietspacebackend.model.request.ChatRequest;
import dev.thural.quietspacebackend.model.response.ChatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChatMapper {

    @Mapping(target = "id", ignore = true)
    Chat chatRequestToEntity(ChatRequest chatRequest);

    ChatResponse chatEntityToResponse(Chat chat);
}