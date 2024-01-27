package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentService {
    Page<CommentDTO> getAllByPost(UUID postId, Integer pageNumber, Integer pageSize);

    CommentDTO addOne(CommentDTO comment, String authHeader);

    Optional<CommentDTO> getById(UUID id);

    void updateOne(UUID commentId, CommentDTO comment, String authHeader);

    void deleteOne(UUID id, String authHeader);

    void patchOne(UUID id, CommentDTO comment, String authHeader);

    void toggleCommentLike(String authHeader, UUID commentId);

    List<CommentLikeDTO> getAllByCommentId(UUID commentId);

    List<CommentLikeDTO> getAllByUserId(UUID userId);
}
