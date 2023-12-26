package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentLikeService {
    void toggleCommentLike(String jwtToken, CommentLikeDTO commentLike);

    List<CommentLikeDTO> getAllByCommentId(UUID commentId);

    List<CommentLikeDTO> getAllByUserId(UUID userId);
}