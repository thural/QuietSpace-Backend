package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.Comment;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<Comment> getAll();
    Comment addOne(Comment comment);

    Optional<Comment> getById(ObjectId id);

    void updateOne(ObjectId id, Comment comment);
}
