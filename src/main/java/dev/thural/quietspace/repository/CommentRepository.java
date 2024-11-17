package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findAllByPostId(UUID postId, Pageable pageable);

    Integer countByParentIdAndPost(UUID parentId, Post post);

    @Transactional
    void deleteAllByParentId(UUID parentId);

    Page<Comment> findAllByParentId(UUID commentId, Pageable pageable);

    Page<Comment> findAllByUserId(UUID userId, PageRequest pageRequest);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.post.id = :postId " +
            "AND c.user.id = :userId " +
            "ORDER BY c.updateDate DESC, c.createDate DESC " +
            "LIMIT 1")
    Optional<Comment> findLatestCommentByPostAndUserByUpdateDate(UUID postId, UUID userId);
}
