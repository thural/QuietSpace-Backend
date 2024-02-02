package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.CommentDto;
import dev.thural.quietspacebackend.model.CommentLikeDto;
import dev.thural.quietspacebackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
    Page<CommentDto> getCommentsByPostId(@PathVariable("postId") UUID postId,
                                         @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                         @RequestParam(name = "page-size", required = false) Integer pageSize) {

        return commentService.getCommentsByPost(postId, pageNumber, pageSize);
    }

    @RequestMapping(COMMENT_PATH_ID)
    CommentDto getCommentById(@PathVariable("commentId") UUID commentId) {

        Optional<CommentDto> optionalComment = commentService.getCommentById(commentId);
        return optionalComment.orElse(null);
    }

    @RequestMapping(value = COMMENT_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createComment(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody @Validated CommentDto comment) {

        CommentDto savedComment = commentService.createComment(comment);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", COMMENT_PATH + "/" + savedComment.getId());
        return new ResponseEntity<>(savedComment, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity<?> putComment(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable("commentId") UUID commentId,
                                 @RequestBody @Validated CommentDto comment) {

        commentService.updateComment(commentId, comment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String authHeader,
                                    @PathVariable("commentId") UUID commentId) {

        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity<?> patchComment(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("commentId") UUID commentId,
                                   @RequestBody CommentDto comment) {

        commentService.patchComment(commentId, comment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID + "/likes", method = RequestMethod.GET)
    List<CommentLikeDto> getCommentLikesByCommentId(@PathVariable("commentId") UUID commentId) {

        return commentService.getLikesByCommentId(commentId);
    }

    @RequestMapping(value = COMMENT_PATH_ID + "/toggle-like", method = RequestMethod.POST)
    ResponseEntity<?> toggleCommentLike(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("commentId") UUID commentId) {

        commentService.toggleCommentLike(commentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
