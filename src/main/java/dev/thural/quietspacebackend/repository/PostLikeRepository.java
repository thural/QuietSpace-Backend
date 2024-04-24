package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.PostLike;
import dev.thural.quietspacebackend.model.response.PostLikeResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    List<PostLikeResponse> findAllByPostId(UUID postId);

    List<PostLikeResponse> findAllByUserId(UUID userId);

    List<PostLikeResponse> findAllByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID likePostId, UUID likeUserId);
}