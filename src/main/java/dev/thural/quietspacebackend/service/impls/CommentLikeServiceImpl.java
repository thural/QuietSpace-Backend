package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import dev.thural.quietspacebackend.repository.CommentLikeRepository;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.CommentLikeService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public void toggleCommentLike(String jwtToken, CommentLikeDTO commentLike) {
        UserEntity loggedUser = getUserEntityByToken(jwtToken);

        if(!commentLike.getUserId().equals(loggedUser.getId()))
            throw new AccessDeniedException("comment like does not belong to logged user");

        UUID likeUserId = commentLike.getUserId();
        UUID likeCommentId = commentLike.getCommentId();

        boolean isCommentLikeExists = commentLikeRepository.existsByCommentIdAndUserId(likeCommentId, likeUserId);

        if (isCommentLikeExists){
            commentLikeRepository.deleteById(commentLike.getId());
        } else {
            UserEntity userEntity = userRepository.findById(likeUserId).orElseThrow(NotFoundException::new);
            CommentEntity commentEntity = commentRepository.findById(likeCommentId).orElseThrow(NotFoundException::new);

            CommentLikeEntity commentLikeEntity = new CommentLikeEntity();

            commentLikeEntity.setUser(userEntity);
            commentLikeEntity.setComment(commentEntity);

            commentLikeRepository.save(commentLikeEntity);
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

    private UserEntity getUserEntityByToken(String jwtToken) {
        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(jwtToken);
        return userRepository.findUserEntityByEmail(loggedUserEmail).orElseThrow(NotFoundException::new);
    }
}
