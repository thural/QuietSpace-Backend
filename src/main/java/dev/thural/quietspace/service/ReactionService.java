package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.LikeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionService {

    void handleReaction(ReactionRequest reaction);

    Optional<ReactionResponse> getUserReactionByContentId(UUID contentId);

    List<ReactionResponse> getReactionsByContentIdAndLikeType(UUID contentId, LikeType likeType);

    Integer getLikeCountByContentIdAndLikeType(UUID contentId, LikeType likeType);

    List<ReactionResponse> getReactionsByContentId(UUID contentId, ContentType type);

    List<ReactionResponse> getReactionsByUserId(UUID userId, ContentType contentType);

}
