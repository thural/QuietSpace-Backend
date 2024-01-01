package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {
    Page<CommentEntity> findAllByPostId(UUID postId, Pageable pageable);
}
