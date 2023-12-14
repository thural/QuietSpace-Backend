package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.model.CommentDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentDTO, UUID> {
}
