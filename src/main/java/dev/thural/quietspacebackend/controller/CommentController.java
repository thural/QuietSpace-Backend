package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.service.CommentService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    Comment getCommentById(@PathVariable("commentId") ObjectId id) {
        Optional<Comment> optionalComment = commentService.getById(id);
        Comment foundComment = optionalComment.orElse(null);
        return foundComment;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    ResponseEntity createComment(@RequestBody Comment comment) {
        Comment savedComment = commentService.addOne(comment);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/comments" + "/" + savedComment.getId());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{commentId}", method = RequestMethod.PUT)
    ResponseEntity putComment(@PathVariable("commentId") ObjectId id, @RequestBody Comment comment) {
        commentService.updateOne(id, comment);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/{commentId}", method = RequestMethod.DELETE)
    ResponseEntity deleteComment(@PathVariable("commentId") ObjectId id) {
        commentService.deleteOne(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
