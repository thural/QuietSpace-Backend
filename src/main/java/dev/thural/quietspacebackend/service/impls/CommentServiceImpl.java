package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.CommentMapper;
import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final CommentRepository commentRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Page<CommentDTO> getAll(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return commentRepository.findAll(pageRequest).map(commentMapper::commentEntityToDto);
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
    public Optional<CommentDTO> getById(UUID id) {
        CommentEntity commentEntity = commentRepository.findById(id).orElseThrow(NotFoundException::new);
        CommentDTO commentDTO = commentMapper.commentEntityToDto(commentEntity);
        return Optional.of(commentDTO);
    }

    @Override
    public void updateOne(UUID id, CommentDTO comment) {
        CommentEntity commentEntity = commentRepository.findById(id).orElse(null);
        CommentDTO commentDTO = commentMapper.commentEntityToDto(commentEntity);
        commentDTO.setUserId(comment.getUserId());
        commentDTO.setText(comment.getText());
        commentRepository.save(commentMapper.commentDtoToEntity(commentDTO));
    }

    @Override
    public void deleteOne(UUID id) {
        commentRepository.deleteById(id);
    }

    @Override
    public void patchOne(UUID id, CommentDTO comment) {
        CommentEntity commentEntity = commentRepository.findById(id).orElse(null);
        CommentDTO commentDTO = commentMapper.commentEntityToDto(commentEntity);
        if (comment.getUserId() != null)
            commentDTO.setUserId(comment.getUserId());
        if (StringUtils.hasText(comment.getText()))
            commentDTO.setText(comment.getText());
        commentRepository.save(commentMapper.commentDtoToEntity(commentDTO));
    }

}
