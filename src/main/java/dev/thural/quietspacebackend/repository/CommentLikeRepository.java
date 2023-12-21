package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLikeEntity, UUID> {

    boolean existsByUserAndComment(UserEntity user, CommentEntity comment);

    List<CommentLikeEntity> getAllByUser(UserEntity userEntity);

    List<CommentLikeEntity> getAllByComment(CommentEntity comment);
}