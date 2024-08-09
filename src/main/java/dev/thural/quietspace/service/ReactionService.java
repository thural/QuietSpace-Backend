package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.ReactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionService {

    void handleReaction(ReactionRequest reaction);

    Optional<ReactionResponse> getUserReactionByContentId(UUID contentId);

    List<ReactionResponse> getReactionsByContentIdAndReactionType(UUID contentId, ReactionType reactionType);

    Integer getLikeCountByContentIdAndReactionType(UUID contentId, ReactionType reactionType);

    List<ReactionResponse> getReactionsByContentIdAndContentType(UUID contentId, ContentType type);

    List<ReactionResponse> getReactionsByUserIdAndContentType(UUID userId, ContentType contentType);

}
