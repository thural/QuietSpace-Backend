package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.utils.enums.ContentType;
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
    private final ReactionService reactionService;

    @GetMapping
    Page<PostResponse> getAllPosts(@RequestParam(name = "page-number", required = false) Integer pageNumber,
                                   @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getAllPosts(pageNumber, pageSize);
    }

    @PostMapping
    ResponseEntity<?> createPost(@RequestBody @Validated PostRequest post) {
        PostResponse createdPost = postService.addPost(post);
        return new ResponseEntity<>(createdPost,HttpStatus.CREATED);
    }

    @GetMapping(POST_PATH_ID)
    ResponseEntity<?> getPostById(@PathVariable UUID postId) {
        Optional<PostResponse> optionalPost = postService.getPostById(postId);
        return new ResponseEntity<>(optionalPost.orElse(null), HttpStatus.OK);
    }

    @PutMapping(POST_PATH_ID)
    ResponseEntity<?> putPost(@PathVariable UUID postId,
                              @RequestBody @Validated PostRequest post) {
        PostResponse updatedPost = postService.updatePost(postId, post);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
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
    List<ReactionResponse> getAllLikesByPostId(@PathVariable UUID postId) {
        return reactionService.getReactionsByContentId(postId, ContentType.POST);
    }

    @PostMapping("/toggle-reaction")
    ResponseEntity<?> togglePostLike(@RequestBody ReactionRequest reaction) {
        reactionService.handleReaction(reaction);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/vote-poll")
    ResponseEntity<?> votePoll(@RequestBody VoteRequest voteRequest) {
        postService.votePostPoll(voteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}