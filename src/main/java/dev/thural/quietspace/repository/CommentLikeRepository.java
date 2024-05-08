package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.CommentLike;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.response.CommentLikeResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    boolean existsByUserAndComment(User user, Comment comment);

    List<CommentLike> getAllByUser(User user);

    List<CommentLike> getAllByComment(Comment comment);

    boolean existsByCommentIdAndUserId(UUID likeCommentId, UUID likeUserId);

    List<CommentLikeResponse> findAllByCommentId(UUID commentId);

    List<CommentLikeResponse> findAllByUserId(UUID userId);

    CommentLike findByCommentIdAndUserId(UUID likeCommentId, UUID likeUserId);
}