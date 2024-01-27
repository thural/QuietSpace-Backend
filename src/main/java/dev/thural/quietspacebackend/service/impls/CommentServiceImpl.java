package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.CommentMapper;
import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import dev.thural.quietspacebackend.repository.CommentLikeRepository;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.service.CommentService;
import dev.thural.quietspacebackend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspacebackend.utils.CustomPageProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public Page<CommentDTO> getAllByPost(UUID postId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return commentRepository.findAllByPostId(postId, pageRequest).map(commentMapper::commentEntityToDto);
    }

    @Override
    public CommentDTO addOne(CommentDTO comment, String authHeader) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));

        Optional<PostEntity> foundPost = postRepository.findById(comment.getPostId());

        if (!loggedUserEntity.getId().equals(comment.getUserId()))
            throw new AccessDeniedException("resource does not belong to current user");
        if (foundPost.isEmpty())
            throw new EntityNotFoundException("post does not exist");

        CommentEntity commentEntity = commentMapper.commentDtoToEntity(comment);
        commentEntity.setUser(loggedUserEntity);
        commentEntity.setPost(foundPost.orElse(null));
        return commentMapper.commentEntityToDto(commentRepository.save(commentEntity));
    }

    @Override
    public Optional<CommentDTO> getById(UUID commentId) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);
        CommentDTO commentDTO = commentMapper.commentEntityToDto(commentEntity);
        return Optional.of(commentDTO);
    }

    @Override
    public void updateOne(UUID commentId, CommentDTO comment, String authHeader) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));
        CommentEntity existingCommentEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingCommentEntity.getUser().equals(loggedUserEntity)) {

            existingCommentEntity.setText(comment.getText());

            commentRepository.save(existingCommentEntity);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void deleteOne(UUID commentId, String authHeader) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));
        CommentEntity existingCommentEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingCommentEntity.getUser().getId().equals(loggedUserEntity.getId())) {
            commentRepository.deleteById(commentId);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

    @Override
    public void patchOne(UUID commentId, CommentDTO comment, String authHeader) {
        UserEntity loggedUserEntity = userService.findUserByJwt(authHeader)
                .orElseThrow(() -> new UserNotFoundException("logged user was not found"));

        CommentEntity existingCommentEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);

        if (existingCommentEntity.getUser().equals(loggedUserEntity)) {
            if (StringUtils.hasText(comment.getText())) existingCommentEntity.setText(comment.getText());
            commentRepository.save(existingCommentEntity);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void toggleCommentLike(String authHeader, UUID commentId) {
        UserEntity user = userService.findUserByJwt(authHeader)
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
    public List<CommentLikeDTO> getAllByCommentId(UUID commentId) {
        return commentLikeRepository.findAllByCommentId(commentId);
    }

    @Override
    public List<CommentLikeDTO> getAllByUserId(UUID userId) {
        return commentLikeRepository.findAllByUserId(userId);
    }

}
