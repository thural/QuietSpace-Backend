package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.PostLikeResponse;
import dev.thural.quietspace.service.PostService;
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
@RequestMapping( "/api/v1/posts")
public class PostController {

    public static final String POST_PATH_ID = "/{postId}";

    private final PostService postService;

    @GetMapping
    Page<PostResponse> getAllPosts(@RequestParam(name = "page-number", required = false) Integer pageNumber,
                                   @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getAllPosts(pageNumber, pageSize);
    }

    @PostMapping
    ResponseEntity<?> createPost(@RequestBody @Validated PostRequest post) {
        postService.addPost(post);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(POST_PATH_ID)
    ResponseEntity<?> getPostById(@PathVariable UUID postId) {
        Optional<PostResponse> optionalPost = postService.getPostById(postId);
        return new ResponseEntity<>(optionalPost.orElse(null), HttpStatus.OK);
    }

    @PutMapping(POST_PATH_ID)
    ResponseEntity<?> putPost(@PathVariable UUID postId,
                              @RequestBody @Validated PostRequest post) {
        postService.updatePost(postId, post);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(POST_PATH_ID)
    ResponseEntity<?> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(POST_PATH_ID)
    ResponseEntity<?> patchPost(@PathVariable UUID postId,
                                @RequestBody PostRequest post) {
        postService.patchPost(postId, post);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(POST_PATH_ID + "/likes")
    List<PostLikeResponse> getAllLikesByPostId(@PathVariable UUID postId) {
        return postService.getPostLikesByPostId(postId);
    }

    @PostMapping(POST_PATH_ID + "/toggle-like")
    ResponseEntity<?> togglePostLike(@PathVariable UUID postId) {
        postService.togglePostLike(postId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}