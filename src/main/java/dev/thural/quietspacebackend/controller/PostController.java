package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class PostController {

    public static final String POST_PATH = "/api/v1/posts";
    public static final String POST_PATH_ID = POST_PATH + "/{postId}";

    private final PostService postService;

    @Autowired
    PostController(PostService postService) {
        this.postService = postService;
    }

    @RequestMapping(value = POST_PATH, method = RequestMethod.GET)
    List<PostDTO> getAllPosts() {
        return postService.getAll();
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.GET)
    PostDTO getPostById(@PathVariable("postId") UUID id) {
        Optional<PostDTO> optionalPost = postService.getById(id);
        PostDTO foundPost = optionalPost.orElse(null);
        return foundPost;
    }

    @RequestMapping(value = POST_PATH, method = RequestMethod.POST)
    ResponseEntity createPost(@RequestBody PostDTO post) {
        PostDTO savedPost = postService.addOne(post);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", POST_PATH + "/" + savedPost.getId());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity putPost(@PathVariable("postId") UUID id, @RequestBody PostDTO post) {
        postService.updateOne(id, post);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity deletePost(@PathVariable("postId") UUID id) {
        postService.deleteOne(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity patchPost(@PathVariable("postId") UUID id, @RequestBody PostDTO post) {
        postService.patchOne(id, post);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
