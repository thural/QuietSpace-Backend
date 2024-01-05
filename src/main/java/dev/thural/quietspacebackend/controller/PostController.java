package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PostController {

    public static final String POST_PATH = "/api/v1/posts";
    public static final String POST_PATH_ID = POST_PATH + "/{postId}";

    private final PostService postService;

    @RequestMapping(value = POST_PATH, method = RequestMethod.GET)
    Page<PostDTO> getAllPosts(@RequestParam(name = "page-number", required = false) Integer pageNumber,
                              @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getAll(pageNumber, pageSize);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.GET)
    PostDTO getPostById(@PathVariable("postId") UUID id) {
        Optional<PostDTO> optionalPost = postService.getById(id);
        PostDTO foundPost = optionalPost.orElse(null);
        return foundPost;
    }

    @RequestMapping(value = POST_PATH, method = RequestMethod.POST)
    ResponseEntity createPost(@RequestHeader("Authorization") String jwtToken, @RequestBody @Validated PostDTO post) {
        PostDTO savedPost = postService.addOne(post, jwtToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", POST_PATH + "/" + savedPost.getId());
        System.out.println("post resource location" + headers.get("Location"));
        return new ResponseEntity(savedPost, headers, HttpStatus.OK);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity putPost(@RequestHeader("Authorization") String jwtToken,
                           @PathVariable("postId") UUID id,
                           @RequestBody @Validated PostDTO post) {
        postService.updateOne(id, post, jwtToken);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity deletePost(@RequestHeader("Authorization") String jwtToken, @PathVariable("postId") UUID id) {
        postService.deleteOne(id, jwtToken);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = POST_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity patchPost(@RequestHeader("Authorization") String jwtToken, @PathVariable("postId") UUID id, @RequestBody PostDTO post) {
        postService.patchOne(jwtToken, id, post);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
