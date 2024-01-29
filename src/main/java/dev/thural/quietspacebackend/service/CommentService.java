package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.CommentDto;
import dev.thural.quietspacebackend.model.CommentLikeDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentService {
    Page<CommentDto> getAllByPost(UUID postId, Integer pageNumber, Integer pageSize);

    CommentDto addOne(CommentDto comment, String authHeader);

    Optional<CommentDto> getById(UUID id);

    void updateOne(UUID commentId, CommentDto comment, String authHeader);

    void deleteOne(UUID id, String authHeader);

    void patchOne(UUID id, CommentDto comment, String authHeader);

    void toggleCommentLike(String authHeader, UUID commentId);

    List<CommentLikeDto> getAllByCommentId(UUID commentId);

    List<CommentLikeDto> getAllByUserId(UUID userId);
}
