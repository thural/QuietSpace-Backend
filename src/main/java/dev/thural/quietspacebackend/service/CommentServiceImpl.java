package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<CommentDTO> getAll() {
        return commentRepository.findAll();
    }

    @Override
    public CommentDTO addOne(CommentDTO comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Optional<CommentDTO> getById(UUID id) {
        return commentRepository.findById(id);
    }

    @Override
    public void updateOne(UUID id, CommentDTO comment) {
        Optional<CommentDTO> optionalComment = commentRepository.findById(id);
        CommentDTO foundComment = optionalComment.get();
        foundComment.setUserId(comment.getUserId());
        foundComment.setText(comment.getText());
        foundComment.setLikes(comment.getLikes());
        commentRepository.save(foundComment);
    }

    @Override
    public void deleteOne(UUID id) {
        commentRepository.deleteById(id);
    }

    @Override
    public void patchOne(UUID id, CommentDTO comment) {
        Optional<CommentDTO> optionalComment = commentRepository.findById(id);
        CommentDTO foundComment = optionalComment.get();
        if (comment.getUserId() != null)
            foundComment.setUserId(comment.getUserId());
        if (StringUtils.hasText(comment.getText()))
            foundComment.setText(comment.getText());
        if (comment.getLikes() != null)
            foundComment.setLikes(comment.getLikes());
        commentRepository.save(foundComment);
    }

}
