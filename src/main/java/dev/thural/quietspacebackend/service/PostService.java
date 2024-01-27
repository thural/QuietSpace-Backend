package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface PostService {
    Page<PostDTO> getAllPosts(Integer pageNumber, Integer pageSize);

    PostDTO addPost(PostDTO post, String authHeader);

    Optional<PostDTO> getPostById(UUID id);

    void updatePost(UUID id, PostDTO post, String authHeader);

    void deletePost(UUID id, String authHeader);

    void patchPost(String authHeader, UUID id, PostDTO post);

    Page<PostDTO> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize);
}
