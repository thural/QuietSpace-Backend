package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.mapper.custom.CommentMapper;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.model.response.CommentResponse;
import dev.thural.quietspace.repository.CommentRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.CommentService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.BY_CREATED_DATE_ASC;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public Page<CommentResponse> getCommentsByPostId(UUID postId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, BY_CREATED_DATE_ASC);
        return commentRepository.findAllByPostId(postId, pageRequest).map(commentMapper::commentEntityToResponse);
    }

    @Override
    public Page<CommentResponse> getCommentsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {

        if (!userService.getLoggedUser().getId().equals(userId))
            throw new UnauthorizedException("user has no access to requested resource");

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        return commentRepository.findAllByUserId(userId, pageRequest).map(commentMapper::commentEntityToResponse);
    }

    @Override
    public CommentResponse createComment(CommentRequest comment) {
        User loggedUser = userService.getLoggedUser();

        Optional<Post> foundPost = postRepository.findById(comment.getPostId());

        if (!loggedUser.getId().equals(comment.getUserId()))
            throw new UnauthorizedException("resource does not belong to current user");
        if (foundPost.isEmpty())
            throw new EntityNotFoundException("post does not exist");

        Comment commentEntity = commentMapper.commentRequestToEntity(comment);
        return commentMapper.commentEntityToResponse(commentRepository.save(commentEntity));
    }

    @Override
    public Optional<CommentResponse> getCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        CommentResponse commentResponse = commentMapper.commentEntityToResponse(comment);
        return Optional.of(commentResponse);
    }

    @Override
    public CommentResponse updateComment(UUID commentId, CommentRequest comment) {
        User loggedUser = userService.getLoggedUser();
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().equals(loggedUser)) {
            existingComment.setText(comment.getText());
            Comment patchedComment = commentRepository.save(existingComment);
            return commentMapper.commentEntityToResponse(patchedComment);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        User loggedUser = userService.getLoggedUser();

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().getId().equals(loggedUser.getId())) {
            log.info("deleting comment {}", existingComment.getId());
            if (existingComment.getParentId() != null)
                commentRepository.deleteAllByParentId(existingComment.getParentId());
            commentRepository.deleteById(commentId);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

    @Override
    public Page<CommentResponse> getRepliesByParentId(UUID commentId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        return commentRepository.findAllByParentId(commentId, pageRequest).map(commentMapper::commentEntityToResponse);
    }

    @Override
    public CommentResponse patchComment(UUID commentId, CommentRequest comment) {
        User loggedUser = userService.getLoggedUser();

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().equals(loggedUser)) {
            if (StringUtils.hasText(comment.getText())) existingComment.setText(comment.getText());
            Comment patchedComment = commentRepository.save(existingComment);
            return commentMapper.commentEntityToResponse(patchedComment);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

}
