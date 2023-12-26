package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface PostService {
    Page<PostDTO> getAll(Integer pageNumber, Integer pageSize);

    PostDTO addOne(PostDTO post, String jwtToken);

    Optional<PostDTO> getById(UUID id);

    void updateOne(UUID id, PostDTO post, String jwtToken);

    void deleteOne(UUID id, String jwtToken);

    void patchOne(String jwtToken, UUID id, PostDTO post);

    Page<PostDTO> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize);
}
