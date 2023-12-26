package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, UUID> {

    List<PostLikeDTO> findAllByPostId(UUID postId);

    List<PostLikeDTO> findAllByUserId(UUID userId);

    List<PostLikeDTO> findAllByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID likePostId, UUID likeUserId);
}