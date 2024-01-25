package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface PostService {
    Page<PostDTO> getAll(Integer pageNumber, Integer pageSize);

    PostDTO addOne(PostDTO post, String authHeader);

    Optional<PostDTO> getById(UUID id);

    void updateOne(UUID id, PostDTO post, String authHeader);

    void deleteOne(UUID id, String authHeader);

    void patchOne(String authHeader, UUID id, PostDTO post);

    Page<PostDTO> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize);
}
