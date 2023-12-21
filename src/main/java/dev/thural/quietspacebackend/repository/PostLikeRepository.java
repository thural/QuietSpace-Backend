package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, UUID> {
    boolean existsByUserAndPost(UserEntity user, PostEntity post);

    List <PostLikeEntity> getAllByUser(UserEntity user);

    List<PostLikeEntity> getAllByPost(PostEntity post);

}