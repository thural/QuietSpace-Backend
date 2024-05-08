package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.response.CommentLikeResponse;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.PostLikeResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    public static final String USER_PATH = "/api/v1/users";
    public static final String USER_PATH_ID = "/{userId}";

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping
    Page<UserResponse> listUsers(@RequestParam(name = "username", required = false) String username,
                                 @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                 @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return userService.listUsers(username, pageNumber, pageSize);
    }

    @GetMapping("/search")
    Page<UserResponse> listUsersByQuery(@RequestParam(name = "query", required = false) String query,
                                        @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                        @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return userService.listUsersByQuery(query, pageNumber, pageSize);
    }

    @GetMapping(USER_PATH_ID)
    ResponseEntity<?> getUserById(@PathVariable UUID userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok().headers(headers).body(user))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(USER_PATH_ID)
    ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable UUID userId) {
        userService.deleteUser(userId, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping
    ResponseEntity<?> patchUser(@RequestBody UserRequest userRequest) {
        userService.patchUser(userRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/profile")
    public UserResponse getUserFromToken() {
        return userService.findLoggedUser().orElse(null);
    }

    @GetMapping(USER_PATH_ID + "/posts")
    public Page<PostResponse> listUserPosts(@PathVariable UUID userId,
                                            @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                            @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getPostsByUserId(userId, pageNumber, pageSize);
    }

    @GetMapping(USER_PATH_ID + "/post-likes")
    List<PostLikeResponse> getAllPostLikesByUserId(@PathVariable UUID userId) {
        return postService.getPostLikesByUserId(userId);
    }

    @GetMapping(USER_PATH_ID + "/comment-likes")
    List<CommentLikeResponse> getAllCommentLikesByUserId(@PathVariable UUID userId) {
        return commentService.getAllByUserId(userId);
    }

}
