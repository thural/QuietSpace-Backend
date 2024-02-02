package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostDto;
import dev.thural.quietspacebackend.model.PostLikeDto;
import dev.thural.quietspacebackend.service.PostService;
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
public class PostController {

    public static final String POST_PATH = "/api/v1/posts";
    public static final String POST_PATH_ID = POST_PATH + "/{postId}";

    private final PostService postService;

    @RequestMapping(value = POST_PATH, method = RequestMethod.GET)
    Page<PostDto> getAllPosts(@RequestParam(name = "page-number", required = false) Integer pageNumber,
                              @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getAllPosts(pageNumber, pageSize);
    }

    @RequestMapping(value = POST_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createPost(@RequestBody @Validated PostDto post) {
        PostDto savedPost = postService.addPost(post);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", POST_PATH + "/" + savedPost.getId());
        System.out.println("post resource location" + headers.get("Location"));
        return new ResponseEntity<>(savedPost, headers, HttpStatus.OK);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.GET)
    PostDto getPostById(@PathVariable("postId") UUID id) {
        Optional<PostDto> optionalPost = postService.getPostById(id);
        return optionalPost.orElse(null);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity<?> putPost(@PathVariable("postId") UUID id,
                              @RequestBody @Validated PostDto post) {
        postService.updatePost(id, post);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deletePost(@PathVariable("postId") UUID id) {
        postService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity<?> patchPost(@PathVariable("postId") UUID id,
                                @RequestBody PostDto post) {
        postService.patchPost(id, post);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID + "/likes", method = RequestMethod.GET)
    List<PostLikeDto> getAllLikesByPostId(@PathVariable("postId") UUID postId) {
        return postService.getPostLikesByPostId(postId);
    }

    @RequestMapping(value = POST_PATH_ID + "/toggle-like", method = RequestMethod.POST)
    ResponseEntity<?> togglePostLike(@PathVariable UUID postId) {
        postService.togglePostLike(postId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}