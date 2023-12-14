package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.CommentDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentService {
    List<CommentDTO> getAll();

    CommentDTO addOne(CommentDTO comment);

    Optional<CommentDTO> getById(UUID id);

    void updateOne(UUID id, CommentDTO comment);

    void deleteOne(UUID id);

    void patchOne(UUID id, CommentDTO comment);
}
