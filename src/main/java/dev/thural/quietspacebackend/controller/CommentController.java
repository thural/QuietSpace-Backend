package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Comment;
import dev.thural.quietspacebackend.repository.CommentRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class CommentController {

    private final CommentRepository commentRepository;

    CommentController(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<Comment> getAll(){
        return commentRepository.findAll();
    }
}
