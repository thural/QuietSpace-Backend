package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<Comment> getAll() {
        return commentService.getAll();
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    Comment createComment(@RequestBody Comment comment) {
        return commentService.addOne(comment);
    }
}
