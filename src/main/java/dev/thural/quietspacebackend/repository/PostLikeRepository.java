package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.model.PostLikeDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, UUID> {

    List<PostLikeDto> findAllByPostId(UUID postId);

    List<PostLikeDto> findAllByUserId(UUID userId);

    List<PostLikeDto> findAllByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID likePostId, UUID likeUserId);
}