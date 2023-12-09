package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.repository.CommentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    @Override
    public Comment addOne(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Optional<Comment> getById(ObjectId id) {
        return commentRepository.findById(id);
    }

    @Override
    public void updateOne(ObjectId id, Comment comment) {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        Comment foundComment = optionalComment.get();
        foundComment.setUserId(comment.getUserId());
        foundComment.setText(comment.getText());
        foundComment.setLikes(comment.getLikes());
        commentRepository.save(foundComment);
    }

    @Override
    public void deleteOne(ObjectId id) {
        commentRepository.deleteById(id);
    }

    @Override
    public void patchOne(ObjectId id, Comment comment) {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        Comment foundComment = optionalComment.get();
        if (comment.getUserId() != null)
            foundComment.setUserId(comment.getUserId());
        if (StringUtils.hasText(comment.getText()))
            foundComment.setText(comment.getText());
        if (comment.getLikes() != null)
            foundComment.setLikes(comment.getLikes());
        commentRepository.save(foundComment);
    }

}
