package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
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
    Page<CommentDTO> getAllCommentsByPostId(@PathVariable("postId") UUID postId,
                                            @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                            @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return commentService.getAllByPost(postId, pageNumber, pageSize);
    }

    @RequestMapping(COMMENT_PATH_ID)
    CommentDTO getCommentById(@PathVariable("commentId") UUID commentId) {
        Optional<CommentDTO> optionalComment = commentService.getById(commentId);
        return optionalComment.orElse(null);
    }

    @RequestMapping(value = COMMENT_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createComment(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody @Validated CommentDTO comment) {
        CommentDTO savedComment = commentService.addOne(comment, authHeader);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", COMMENT_PATH + "/" + savedComment.getId());
        return new ResponseEntity<>(savedComment, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity<?> putComment(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable("commentId") UUID commentId,
                                 @RequestBody @Validated CommentDTO comment) {
        commentService.updateOne(commentId, comment, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String authHeader,
                                    @PathVariable("commentId") UUID commentId) {
        commentService.deleteOne(commentId, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity<?> patchComment(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("commentId") UUID commentId,
                                   @RequestBody CommentDTO comment) {
        commentService.patchOne(commentId, comment, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID + "/likes", method = RequestMethod.GET)
    List<CommentLikeDTO> getAllCommentLikesByCommentId(@PathVariable("commentId") UUID commentId) {
        return commentService.getAllByCommentId(commentId);
    }

    @RequestMapping(value = COMMENT_PATH_ID + "/toggle-like", method = RequestMethod.POST)
    ResponseEntity<?> toggleCommentLike(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("commentId") UUID commentId) {
        commentService.toggleCommentLike(authHeader, commentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
