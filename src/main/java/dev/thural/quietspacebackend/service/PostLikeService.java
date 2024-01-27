package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostLikeDTO;

import java.util.List;
import java.util.UUID;

public interface PostLikeService {
    List<PostLikeDTO> getAllByPostId(UUID postId);

    void togglePostLike(String authHeader, UUID postId);

    List<PostLikeDTO> getAllByUserId(UUID userId);
}
