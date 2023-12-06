package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getAll();
    Comment addOne(Comment comment);
}
