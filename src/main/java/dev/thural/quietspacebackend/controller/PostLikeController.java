package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.service.PostLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostLikeController {

    public static final String POST_LIKE_PATH = "/api/v1/post-like";
    private final PostLikeService postLikeService;

    @Autowired
    PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @RequestMapping(value = POST_LIKE_PATH, method = RequestMethod.GET)
    List<PostLikeDTO> getAllPostLikes() {
        return postLikeService.getAll();
    }

    @RequestMapping(value = POST_LIKE_PATH + "/get-by-post", method = RequestMethod.GET)
    List<PostLikeDTO> getPostLikesByPost(@RequestBody PostDTO post) {
        List<PostLikeDTO> postLikes = postLikeService.getAllByPost(post);
        return postLikes;
    }

    @RequestMapping(value = POST_LIKE_PATH + "/get-by-user", method = RequestMethod.GET)
    List<PostLikeDTO> getPostLikesByUser(@RequestBody UserDTO user) {
        List<PostLikeDTO> postLikes = postLikeService.getAllByUser(user);
        return postLikes;
    }

    @RequestMapping(value = POST_LIKE_PATH, method = RequestMethod.POST)
    ResponseEntity togglePostLike(@RequestBody PostLikeDTO postLike) {
        postLikeService.togglePostLike(postLike);
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
