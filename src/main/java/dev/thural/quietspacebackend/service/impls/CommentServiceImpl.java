package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.mapper.CommentMapper;
import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.repository.CommentRepository;
import dev.thural.quietspacebackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    public List<CommentDTO> getAll() {
        return commentRepository.findAll()
                .stream()
                .map(commentMapper::commentEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDTO addOne(CommentDTO comment) {
        CommentEntity commentEntity = commentMapper.commentDtoToEntity(comment);
        return commentMapper.commentEntityToDto(commentRepository.save(commentEntity));
    }

    @Override
    public Optional<CommentDTO> getById(UUID id) {
        CommentEntity commentEntity = commentRepository.findById(id).orElse(null);
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
