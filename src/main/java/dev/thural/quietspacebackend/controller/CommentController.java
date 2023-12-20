package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class CommentController {

    public static final String COMMENT_PATH = "/api/v1/comments";
    public static final String COMMENT_PATH_ID = COMMENT_PATH + "/{commentId}";

    private final CommentService commentService;

    @Autowired
    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping(value = COMMENT_PATH, method = RequestMethod.GET)
    List<CommentDTO> getAllComments() {
        return commentService.getAll();
    }

    @RequestMapping(COMMENT_PATH_ID)
    CommentDTO getCommentById(@PathVariable("commentId") UUID id) {
        Optional<CommentDTO> optionalComment = commentService.getById(id);
        CommentDTO foundComment = optionalComment.orElseThrow(NotFoundException::new);
        return foundComment;
    }

    @RequestMapping(value = COMMENT_PATH, method = RequestMethod.POST)
    ResponseEntity createComment(@RequestBody CommentDTO comment) {
        CommentDTO savedComment = commentService.addOne(comment);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", COMMENT_PATH + "/" + savedComment.getId());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity putComment(@PathVariable("commentId") UUID id, @RequestBody CommentDTO comment) {
        commentService.updateOne(id, comment);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity deleteComment(@PathVariable("commentId") UUID id) {
        commentService.deleteOne(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = COMMENT_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity patchComment(@PathVariable("commentId") UUID id, @RequestBody CommentDTO comment){
        commentService.patchOne(id, comment);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
