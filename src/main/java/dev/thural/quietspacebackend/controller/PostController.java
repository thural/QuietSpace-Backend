package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.Post;
import dev.thural.quietspacebackend.service.PostService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping("/{postId}")
    Post getPostById(@PathVariable("postId") ObjectId id){
        Optional<Post> optionalPost = postService.getById(id);
        Post foundPost = optionalPost.orElse(null);
        return foundPost;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    Post createPost(@RequestBody Post post) {
        return postService.addOne(post);
    }
}
