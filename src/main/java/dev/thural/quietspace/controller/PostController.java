package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    public static final String POST_PATH = "/api/v1/posts";
    public static final String POST_PATH_ID = "/{postId}";

    private final PostService postService;

    @GetMapping
    Page<PostResponse> getAllPosts(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return postService.getAllPosts(pageNumber, pageSize);
    }

    @GetMapping("/search")
    Page<PostResponse> getPostsByQuery(
            @RequestParam(name = "query", required = true) String query,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return postService.getAllByQuery(query, pageNumber, pageSize);
    }

    @PostMapping
    ResponseEntity<PostResponse> createPost(@RequestBody @Validated PostRequest post) {
        return ResponseEntity.ok(postService.addPost(post));
    }

    @GetMapping(POST_PATH_ID)
    ResponseEntity<PostResponse> getPostById(@PathVariable UUID postId) {
        return postService.getPostById(postId)
                .map(post -> ResponseEntity.ok().body(post))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(POST_PATH_ID)
    ResponseEntity<PostResponse> putPost(@PathVariable UUID postId, @RequestBody @Validated PostRequest post) {
        return ResponseEntity.ok(postService.updatePost(postId, post));
    }

    @DeleteMapping(POST_PATH_ID)
    ResponseEntity<?> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(POST_PATH_ID)
    ResponseEntity<PostResponse> patchPost(@PathVariable UUID postId, @RequestBody PostRequest post) {
        return ResponseEntity.ok(postService.patchPost(postId, post));
    }

    @PostMapping("/vote-poll")
    ResponseEntity<?> votePoll(@RequestBody VoteRequest voteRequest) {
        postService.votePoll(voteRequest);
        return ResponseEntity.ok().build();
    }

}