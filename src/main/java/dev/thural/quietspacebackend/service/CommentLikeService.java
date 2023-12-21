package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentLikeService {
    List<CommentLikeDTO> getAll();

    Optional<CommentLikeDTO> getById(UUID id);

    List<CommentLikeDTO> getAllByUser(UserDTO user);

    List<CommentLikeDTO> getAllByComment(CommentDTO comment);

    void toggleCommentLike(CommentLikeDTO commentLike);
}