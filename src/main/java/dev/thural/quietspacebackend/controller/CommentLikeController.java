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
    List<CommentLikeDTO> getAllPostLikes() {
        return commentLikeService.getAll();
    }

    @RequestMapping(value = COMMENT_LIKE_PATH, method = RequestMethod.GET)
    List<CommentLikeDTO > getCommentLikesByPost(@RequestBody CommentDTO comment) {
        List<CommentLikeDTO> commentLikes = commentLikeService.getAllByComment(comment);
        return commentLikes;
    }

    @RequestMapping(value = COMMENT_LIKE_PATH, method = RequestMethod.GET)
    List<CommentLikeDTO> getCommentLikesByUser(@RequestBody UserDTO user){
        List<CommentLikeDTO> postLikes = commentLikeService.getAllByUser(user);
        return postLikes;
    }

    @RequestMapping(value = COMMENT_LIKE_PATH, method = RequestMethod.POST)
    ResponseEntity togglePostLike(@RequestBody CommentLikeDTO postLike) {
        commentLikeService.toggleCommentLike(postLike);
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
