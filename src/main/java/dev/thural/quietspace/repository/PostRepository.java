package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findAllByUserId(UUID userId, Pageable pageable);
}