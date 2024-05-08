package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.CommentLike;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.CommentMapper;
import dev.thural.quietspace.model.response.CommentLikeResponse;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.repository.CommentLikeRepository;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Override
    public Page<dev.thural.quietspace.model.response.CommentResponse> getCommentsByPost(UUID postId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return commentRepository.findAllByPostId(postId, pageRequest).map(commentMapper::commentEntityToResponse);
    }

    @Override
    public void createComment(CommentRequest comment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Optional<Post> foundPost = postRepository.findById(comment.getPostId());

        if (!loggedUser.getId().equals(comment.getUserId()))
            throw new AccessDeniedException("resource does not belong to current user");
        if (foundPost.isEmpty())
            throw new EntityNotFoundException("post does not exist");

        Comment commentEntity = commentMapper.commentRequestToEntity(comment);
        commentEntity.setUser(loggedUser);
        commentEntity.setPost(foundPost.orElse(null));
        commentMapper.commentEntityToResponse(commentRepository.save(commentEntity));
    }

    @Override
    public Optional<dev.thural.quietspace.model.response.CommentResponse> getCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        dev.thural.quietspace.model.response.CommentResponse commentResponse = commentMapper.commentEntityToResponse(comment);
        return Optional.of(commentResponse);
    }

    @Override
    public void updateComment(UUID commentId, CommentRequest comment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().equals(loggedUser)) {
            existingComment.setText(comment.getText());
            commentRepository.save(existingComment);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void deleteComment(UUID commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().getId().equals(loggedUser.getId())) {
            commentRepository.deleteById(commentId);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

    @Override
    public void patchComment(UUID commentId, CommentRequest comment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().equals(loggedUser)) {
            if (StringUtils.hasText(comment.getText())) existingComment.setText(comment.getText());
            commentRepository.save(existingComment);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void toggleCommentLike(UUID commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        boolean isLikeExists = commentLikeRepository
                .existsByCommentIdAndUserId(commentId, user.getId());

        if (isLikeExists) {
            CommentLike foundLike = commentLikeRepository
                    .findByCommentIdAndUserId(commentId, user.getId());
            commentLikeRepository.deleteById(foundLike.getId());
        } else {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(EntityNotFoundException::new);
            commentLikeRepository.save(CommentLike.builder().comment(comment).user(user).build());
        }
    }

    @Override
    public List<CommentLikeResponse> getLikesByCommentId(UUID commentId) {
        return commentLikeRepository.findAllByCommentId(commentId);
    }

    @Override
    public List<CommentLikeResponse> getAllByUserId(UUID userId) {
        return commentLikeRepository.findAllByUserId(userId);
    }

}
