package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.*;
import dev.thural.quietspacebackend.service.CommentLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommentLikeController {

    public static final String COMMENT_LIKE_PATH = "/api/v1/comment-like";
    private final CommentLikeService commentLikeService;

    @Autowired
    CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @RequestMapping(value = COMMENT_LIKE_PATH, method = RequestMethod.GET)
    List<CommentLikeDTO> getAllCommentLikes() {
        return commentLikeService.getAll();
    }

    @RequestMapping(value = COMMENT_LIKE_PATH + "/get-by-comment", method = RequestMethod.GET)
    List<CommentLikeDTO > getCommentLikesByComment(@RequestBody CommentDTO comment) {
        List<CommentLikeDTO> commentLikes = commentLikeService.getAllByComment(comment);
        return commentLikes;
    }

    @RequestMapping(value = COMMENT_LIKE_PATH + "/get-by-user", method = RequestMethod.GET)
    List<CommentLikeDTO> getCommentLikesByUser(@RequestBody UserDTO user){
        List<CommentLikeDTO> commentLikes = commentLikeService.getAllByUser(user);
        return commentLikes;
    }

    @RequestMapping(value = COMMENT_LIKE_PATH, method = RequestMethod.POST)
    ResponseEntity toggleCommentLike(@RequestBody CommentLikeDTO commentLike) {
        commentLikeService.toggleCommentLike(commentLike);
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
