package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDto;
import dev.thural.quietspacebackend.model.PostLikeDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostService {
    Page<PostDto> getAllPosts(Integer pageNumber, Integer pageSize);

    PostDto addPost(PostDto post);

    Optional<PostDto> getPostById(UUID id);

    void updatePost(UUID id, PostDto post);

    void deletePost(UUID id);

    void patchPost(UUID id, PostDto post);

    Page<PostDto> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize);

    List<PostLikeDto> getPostLikesByPostId(UUID postId);

    void togglePostLike(UUID postId);

    List<PostLikeDto> getPostLikesByUserId(UUID userId);
}
