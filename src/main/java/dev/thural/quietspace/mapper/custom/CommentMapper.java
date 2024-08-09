package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.model.response.CommentResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.repository.ReactionRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.utils.enums.ReactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final ReactionService reactionService;


    public Comment commentRequestToEntity(CommentRequest comment) {
        return Comment.builder()
                .parentId(comment.getParentId())
                .text(comment.getText())
                .user(getUserById(comment.getUserId()))
                .post(getPostById(comment.getPostId()))
                .build();
    }

    ;

    public CommentResponse commentEntityToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .parentId(comment.getParentId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .text(comment.getText())
                .userReaction(getUserReaction(comment.getId()))
                .createDate(comment.getCreateDate())
                .updateDate(comment.getUpdateDate())
                .likeCount(getLikeCount(comment.getId()))
                .replyCount(getReplyCount(comment.getId(), comment.getPost()))
                .build();
    }

    ;

    private Post getPostById(UUID postId) {
        return postRepository.findById(postId).orElse(null);
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private Integer getReplyCount(UUID parentId, Post post) {
        return commentRepository.countByParentIdAndPost(parentId, post);
    }

    private Integer getLikeCount(UUID commentId) {
        return reactionRepository.countByContentIdAndReactionType(commentId, ReactionType.LIKE);
    }

    private ReactionResponse getUserReaction(UUID commentId) {
        return reactionService.getUserReactionByContentId(commentId).orElse(null);
    }

}
