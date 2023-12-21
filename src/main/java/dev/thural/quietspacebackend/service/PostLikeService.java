package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import dev.thural.quietspacebackend.model.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeService {
    List<PostLikeDTO> getAll();

    Optional<PostLikeDTO> getById(UUID id);

    List<PostLikeDTO> getAllByUser(UserDTO user);

    List<PostLikeDTO> getAllByPost(PostDTO post);

    void togglePostLike(PostLikeDTO postLike);
}
