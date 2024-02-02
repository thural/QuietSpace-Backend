package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.CommentMapper;
import dev.thural.quietspacebackend.model.CommentDto;
import dev.thural.quietspacebackend.model.CommentLikeDto;
import dev.thural.quietspacebackend.repository.CommentLikeRepository;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.CommentService;
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

import static dev.thural.quietspacebackend.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CommentDto> getCommentsByPost(UUID postId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return commentRepository.findAllByPostId(postId, pageRequest).map(commentMapper::commentEntityToDto);
    }

    @Override
    public CommentDto createComment(CommentDto comment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Optional<PostEntity> foundPost = postRepository.findById(comment.getPostId());

        if (!loggedUser.getId().equals(comment.getUserId()))
            throw new AccessDeniedException("resource does not belong to current user");
        if (foundPost.isEmpty())
            throw new EntityNotFoundException("post does not exist");

        CommentEntity commentEntity = commentMapper.commentDtoToEntity(comment);
        commentEntity.setUser(loggedUser);
        commentEntity.setPost(foundPost.orElse(null));
        return commentMapper.commentEntityToDto(commentRepository.save(commentEntity));
    }

    @Override
    public Optional<CommentDto> getCommentById(UUID commentId) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        CommentDto commentDTO = commentMapper.commentEntityToDto(commentEntity);
        return Optional.of(commentDTO);
    }

    @Override
    public void updateComment(UUID commentId, CommentDto comment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        CommentEntity existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().equals(loggedUser)) {
            existingComment.setText(comment.getText());
            commentRepository.save(existingComment);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void deleteComment(UUID commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        CommentEntity existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().getId().equals(loggedUser.getId())) {
            commentRepository.deleteById(commentId);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

    @Override
    public void patchComment(UUID commentId, CommentDto comment) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        CommentEntity existingComment = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingComment.getUser().equals(loggedUser)) {
            if (StringUtils.hasText(comment.getText())) existingComment.setText(comment.getText());
            commentRepository.save(existingComment);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void toggleCommentLike(UUID commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        boolean isLikeExists = commentLikeRepository
                .existsByCommentIdAndUserId(commentId, user.getId());

        if (isLikeExists) {
            CommentLikeEntity foundLike = commentLikeRepository
                    .findByCommentIdAndUserId(commentId, user.getId());
            commentLikeRepository.deleteById(foundLike.getId());
        } else {
            CommentEntity comment = commentRepository.findById(commentId)
                    .orElseThrow(EntityNotFoundException::new);
            commentLikeRepository.save(CommentLikeEntity.builder().comment(comment).user(user).build());
        }
    }

    @Override
    public List<CommentLikeDto> getLikesByCommentId(UUID commentId) {
        return commentLikeRepository.findAllByCommentId(commentId);
    }

    @Override
    public List<CommentLikeDto> getAllByUserId(UUID userId) {
        return commentLikeRepository.findAllByUserId(userId);
    }

}
