package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostLikeDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeService {
    Optional<PostLikeDTO> getById(UUID id);

    List<PostLikeDTO> getAllByPostId(UUID postId);

    void togglePostLike(String jwtToken, PostLikeDTO postLikeDTO);

    List<PostLikeDTO> getAllByUserId(UUID userId);

    List<PostLikeDTO> getAllByPostIdAndUserId(UUID postId, UUID userId);
}
