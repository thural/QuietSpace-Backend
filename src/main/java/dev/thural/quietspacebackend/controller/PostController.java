package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Post;
import dev.thural.quietspacebackend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    PostController(PostService postService){
        this.postService = postService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<Post> list(){
        return postService.getAll();
    }

}
