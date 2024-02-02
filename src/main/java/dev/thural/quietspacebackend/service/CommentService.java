package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.CommentDto;
import dev.thural.quietspacebackend.model.CommentLikeDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentService {
    Page<CommentDto> getCommentsByPost(UUID postId, Integer pageNumber, Integer pageSize);

    CommentDto createComment(CommentDto comment);

    Optional<CommentDto> getCommentById(UUID id);

    void updateComment(UUID commentId, CommentDto comment);

    void deleteComment(UUID id);

    void patchComment(UUID id, CommentDto comment);

    void toggleCommentLike(UUID commentId);

    List<CommentLikeDto> getLikesByCommentId(UUID commentId);

    List<CommentLikeDto> getAllByUserId(UUID userId);
}
