package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDto;
import dev.thural.quietspacebackend.model.PostLikeDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostService {
    Page<PostDto> getAllPosts(Integer pageNumber, Integer pageSize);

    PostDto addPost(PostDto post, String authHeader);

    Optional<PostDto> getPostById(UUID id);

    void updatePost(UUID id, PostDto post, String authHeader);

    void deletePost(UUID id, String authHeader);

    void patchPost(String authHeader, UUID id, PostDto post);

    Page<PostDto> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize);

    List<PostLikeDto> getPostLikesByPostId(UUID postId);

    void togglePostLike(String authHeader, UUID postId);

    List<PostLikeDto> getPostLikesByUserId(UUID userId);
}
