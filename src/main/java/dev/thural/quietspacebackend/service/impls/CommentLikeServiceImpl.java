package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import dev.thural.quietspacebackend.repository.CommentLikeRepository;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.service.CommentLikeService;
import dev.thural.quietspacebackend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CommentLikeRepository commentLikeRepository;

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
