package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.FollowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<FollowEntity, UUID> {

    Page<FollowEntity> findAllByFollowerId(UUID followerId, Pageable pageable);

    Page<FollowEntity> findAllByFollowingId(UUID followingId, Pageable pageable);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    boolean deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}