package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {
}