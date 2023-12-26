package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.CommentDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface CommentService {
    Page<CommentDTO> getAll(Integer pageNumber, Integer pageSize);

    CommentDTO addOne(CommentDTO comment, String jwtToken);

    Optional<CommentDTO> getById(UUID id);

    void updateOne(UUID id, CommentDTO comment);

    void deleteOne(UUID id);

    void patchOne(UUID id, CommentDTO comment);
}
