package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.service.CommentService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<Comment> getAllComments() {
        return commentService.getAll();
    }

    @RequestMapping("/{commentId}")
    Comment getCommentById(@PathVariable("commentId") ObjectId id){
        Optional<Comment> optionalComment = commentService.getById(id);
        Comment foundComment = optionalComment.orElse(null);
        return foundComment;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    Comment createComment(@RequestBody Comment comment) {
        return commentService.addOne(comment);
    }
}
