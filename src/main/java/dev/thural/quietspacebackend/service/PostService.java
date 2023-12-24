package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostService {
    List<PostDTO> getAll();

    PostDTO addOne(PostDTO post, String jwtToken);

    Optional<PostDTO> getById(UUID id);

    void updateOne(UUID id, PostDTO post, String jwtToken);

    void deleteOne(UUID id, String jwtToken);

    void patchOne(String jwtToken, UUID id, PostDTO post);
}
