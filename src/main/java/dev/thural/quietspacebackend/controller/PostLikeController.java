package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostLikeDTO;
import dev.thural.quietspacebackend.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PostLikeController {

    public static final String POST_LIKE_PATH = "/api/v1/post-like";
    private final PostLikeService postLikeService;

    @RequestMapping(value = POST_LIKE_PATH + "/posts/{postId}", method = RequestMethod.GET)
    List<PostLikeDTO> getAllPostLikesByPostId(@PathVariable("postId") UUID postId) {
        return postLikeService.getAllByPostId(postId);
    }

    @RequestMapping(value = POST_LIKE_PATH + "/users/{userId}", method = RequestMethod.GET)
    List<PostLikeDTO> getAllPostLikesByUserId(@PathVariable("userId") UUID userId) {
        return postLikeService.getAllByUserId(userId);
    }

    @RequestMapping(value = POST_LIKE_PATH + "/toggle-like", method = RequestMethod.POST)
    ResponseEntity<?> togglePostLike(@RequestHeader("Authorization") String jwtToken,
                                     @RequestBody PostLikeDTO postLikeDTO) {
        postLikeService.togglePostLike(jwtToken, postLikeDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
