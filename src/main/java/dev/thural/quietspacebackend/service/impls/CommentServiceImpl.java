package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.CommentMapper;
import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.CommentService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspacebackend.utils.CustomPageProvider.buildCustomPageRequest;
import static dev.thural.quietspacebackend.utils.JwtProvider.getEmailFromJwtToken;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Page<CommentDTO> getAllByPost(UUID postId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return commentRepository.findAllByPostId(postId,pageRequest).map(commentMapper::commentEntityToDto);
    }

    @Override
    public CommentDTO addOne(CommentDTO comment, String jwtToken) {
        String loggedUserEmail = getEmailFromJwtToken(jwtToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loggedUserEmail);
        CommentEntity commentEntity = commentMapper.commentDtoToEntity(comment);

        commentEntity.setUsername(userDetails.getUsername());
        commentEntity.setText(comment.getText());
        return commentMapper.commentEntityToDto(commentRepository.save(commentEntity));
    }

    @Override
    public Optional<CommentDTO> getById(UUID commentId) {
        CommentEntity commentEntity = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);
        CommentDTO commentDTO = commentMapper.commentEntityToDto(commentEntity);
        return Optional.of(commentDTO);
    }

    @Override
    public void updateOne(UUID commentId, CommentDTO comment, String jwtToken) {
        UserEntity loggedUserEntity = getUserEntityByToken(jwtToken);
        CommentEntity existingCommentEntity = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);

        if (existingCommentEntity.getUser().equals(loggedUserEntity)){

            existingCommentEntity.setUsername(comment.getUsername());
            existingCommentEntity.setText(comment.getText());

            commentRepository.save(existingCommentEntity);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    @Override
    public void deleteOne(UUID commentId, String jwtToken) {
        UserEntity loggedUserEntity = getUserEntityByToken(jwtToken);
        CommentEntity existingCommentEntity = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);

        if (existingCommentEntity.getUser().equals(loggedUserEntity)){
            commentRepository.deleteById(commentId);
        } else throw new AccessDeniedException("comment author does not belong to current user");
    }

    @Override
    public void patchOne(UUID commentId, CommentDTO comment, String jwtToken) {
        UserEntity loggedUserEntity = getUserEntityByToken(jwtToken);
        CommentEntity existingCommentEntity = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);

        if (existingCommentEntity.getUser().equals(loggedUserEntity)){
            if (comment.getUsername() != null) existingCommentEntity.setUsername(comment.getUsername());
            if (StringUtils.hasText(comment.getText())) existingCommentEntity.setText(comment.getText());
            commentRepository.save(existingCommentEntity);
        } else throw new AccessDeniedException("comment author does not belong to current user");

    }

    private UserEntity getUserEntityByToken(String jwtToken) {
        String loggedUserEmail = JwtProvider.getEmailFromJwtToken(jwtToken);
        return userRepository.findUserEntityByEmail(loggedUserEmail).orElseThrow(NotFoundException::new);
    }

}
