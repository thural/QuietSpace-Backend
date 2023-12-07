package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.repository.CommentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;

    @Autowired
    CommentServiceImpl(CommentRepository commentRepository){
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
}
