package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Post;
import dev.thural.quietspacebackend.service.PostService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    PostController(PostService postService) {
        this.postService = postService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<Post> getAllPosts() {
        return postService.getAll();
    }

    @RequestMapping(value = "/{postId}", method = RequestMethod.GET)
    Post getPostById(@PathVariable("postId") ObjectId id){
        Optional<Post> optionalPost = postService.getById(id);
        Post foundPost = optionalPost.orElse(null);
        return foundPost;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    ResponseEntity createPost(@RequestBody Post post) {
        Post savedPost = postService.addOne(post);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/posts" + "/" + savedPost.getId());
        return new  ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "{postId}", method = RequestMethod.PUT)
    ResponseEntity putPost(@PathVariable("postId") ObjectId id, @RequestBody Post post){
        postService.updateOne(id, post);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
