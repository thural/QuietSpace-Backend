package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.response.CommentResponse;
import dev.thural.quietspace.model.response.CommentLikeResponse;
import dev.thural.quietspace.model.request.CommentRequest;
import dev.thural.quietspace.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    public static final String COMMENT_PATH = "/api/v1/comments";
    public static final String COMMENT_PATH_ID = COMMENT_PATH + "/{commentId}";

    private final CommentService commentService;


    @RequestMapping(value = COMMENT_PATH + "/post/{postId}", method = RequestMethod.GET)
    Page<CommentResponse> getCommentsByPostId(@PathVariable("postId") UUID postId,
                                              @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                              @RequestParam(name = "page-size", required = false) Integer pageSize) {

        return commentService.getCommentsByPost(postId, pageNumber, pageSize);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.GET)
    CommentResponse getCommentById(@PathVariable("commentId") UUID commentId) {

        Optional<CommentResponse> optionalComment = commentService.getCommentById(commentId);
        return optionalComment.orElse(null);
    }

    @RequestMapping(value = COMMENT_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createComment(@RequestBody @Validated CommentRequest comment) {

        commentService.createComment(comment);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity<?> putComment(@PathVariable("commentId") UUID commentId,
                                 @RequestBody @Validated CommentRequest comment) {

        commentService.updateComment(commentId, comment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deleteComment(@PathVariable("commentId") UUID commentId) {

        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity<?> patchComment(@PathVariable("commentId") UUID commentId,
                                   @RequestBody CommentRequest comment) {

        commentService.patchComment(commentId, comment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID + "/likes", method = RequestMethod.GET)
    List<CommentLikeResponse> getCommentLikesByCommentId(@PathVariable("commentId") UUID commentId) {

        return commentService.getLikesByCommentId(commentId);
    }

    @RequestMapping(value = COMMENT_PATH_ID + "/toggle-like", method = RequestMethod.POST)
    ResponseEntity<?> toggleCommentLike(@PathVariable("commentId") UUID commentId) {

        commentService.toggleCommentLike(commentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
