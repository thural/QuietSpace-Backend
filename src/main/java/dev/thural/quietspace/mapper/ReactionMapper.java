package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ReactionMapper {
    @Mapping(target = "id", ignore = true)
    Reaction reactionRequestToEntity(ReactionRequest reactionRequest);

    ReactionResponse reactionEntityToResponse(Reaction reaction);
}