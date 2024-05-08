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
@RequestMapping("/api/v1/comments")
public class CommentController {

    public static final String COMMENT_PATH_ID = "/{commentId}";

    private final CommentService commentService;


    @GetMapping("/post/{postId}")
    Page<CommentResponse> getCommentsByPostId(@PathVariable UUID postId,
                                              @RequestParam(required = false) Integer pageNumber,
                                              @RequestParam(required = false) Integer pageSize) {
        return commentService.getCommentsByPost(postId, pageNumber, pageSize);
    }

    @GetMapping(COMMENT_PATH_ID)
    CommentResponse getCommentById(@PathVariable UUID commentId) {
        Optional<CommentResponse> optionalComment = commentService.getCommentById(commentId);
        return optionalComment.orElse(null);
    }

    @PostMapping
    ResponseEntity<?> createComment(@RequestBody @Validated CommentRequest comment) {
        commentService.createComment(comment);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(COMMENT_PATH_ID)
    ResponseEntity<?> putComment(@PathVariable UUID commentId,
                                 @RequestBody @Validated CommentRequest comment) {
        commentService.updateComment(commentId, comment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(COMMENT_PATH_ID)
    ResponseEntity<?> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(COMMENT_PATH_ID)
    ResponseEntity<?> patchComment(@PathVariable UUID commentId,
                                   @RequestBody CommentRequest comment) {
        commentService.patchComment(commentId, comment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = COMMENT_PATH_ID + "/likes")
    List<CommentLikeResponse> getCommentLikesByCommentId(@PathVariable UUID commentId) {
        return commentService.getLikesByCommentId(commentId);
    }

    @PostMapping(COMMENT_PATH_ID + "/toggle-like")
    ResponseEntity<?> toggleCommentLike(@PathVariable UUID commentId) {
        commentService.toggleCommentLike(commentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
