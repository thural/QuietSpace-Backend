package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.*;
import dev.thural.quietspacebackend.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentLikeController {

    public static final String COMMENT_LIKE_PATH = "/api/v1/comment-like";
    private final CommentLikeService commentLikeService;

    @RequestMapping(value = COMMENT_LIKE_PATH + "/comments/{commentId}", method = RequestMethod.GET)
    List<CommentLikeDTO> getAllCommentLikesByCommentId(@PathVariable("commentId") UUID commentId) {
        return commentLikeService.getAllByCommentId(commentId);
    }

    @RequestMapping(value = COMMENT_LIKE_PATH + "/users/{userId}", method = RequestMethod.GET)
    List<CommentLikeDTO> getAllCommentLikesByUserId(@PathVariable("userId") UUID userId) {
        List<CommentLikeDTO> commentLikes = commentLikeService.getAllByUserId(userId);
        return commentLikes;
    }

    @RequestMapping(value = COMMENT_LIKE_PATH + "/toggle-like", method = RequestMethod.POST)
    ResponseEntity toggleCommentLike(@RequestHeader("Authorization") String jwtToken,
                                     @RequestBody @Validated CommentLikeDTO commentLikeDTO) {
        commentLikeService.toggleCommentLike(jwtToken, commentLikeDTO);
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
