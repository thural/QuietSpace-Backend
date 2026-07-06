package dev.thural.quietspace.service;

import dev.thural.quietspace.enums.EntityType;
import dev.thural.quietspace.enums.ReactionType;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ReactionService {

    void handleReaction(ReactionRequest reaction);

    Optional<ReactionResponse> getUserReactionByContentId(UUID contentId);

    Page<ReactionResponse> getReactionsByContentIdAndReactionType(UUID contentId, ReactionType reactionType, Integer pageNumber, Integer pageSize);

    Integer countByContentIdAndReactionType(UUID contentId, ReactionType reactionType);

    Page<ReactionResponse> getReactionsByContentIdAndContentType(UUID contentId, EntityType type, Integer pageNumber, Integer pageSize);

    Page<ReactionResponse> getReactionsByUserIdAndContentType(UUID userId, EntityType contentType, Integer pageNumber, Integer pageSize);

}
