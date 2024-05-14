package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.utils.enums.ContentType;

import java.util.List;
import java.util.UUID;

public interface ReactionService {

    void handleReaction(ReactionRequest reaction);

    List<ReactionResponse> getReactionsByContentId(UUID contentId, ContentType type);

    List<ReactionResponse> getReactionsByUserId(UUID userId, ContentType contentType);

}
