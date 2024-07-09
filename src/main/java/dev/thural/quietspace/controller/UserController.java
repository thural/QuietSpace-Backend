package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.FollowResponse;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.*;
import dev.thural.quietspace.utils.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
    public static final String FOLLOW_PATH_ID = "follow/{userId}";
    public static final String FOLLOW_USER_TOGGLE = FOLLOW_PATH_ID + "/toggle-follow";

    private final UserService userService;
    private final PostService postService;
    private final FollowService followService;
    private final ReactionService reactionService;

    @GetMapping
    Page<UserResponse> listUsers(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return userService.listUsers(username, pageNumber, pageSize);
    }

    @GetMapping("/search")
    Page<UserResponse> listUsersByQuery(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return userService.listUsersByQuery(query, pageNumber, pageSize);
    }

    @GetMapping(USER_PATH_ID)
    ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return userService.getUserResponseById(userId)
                .map(user -> ResponseEntity.ok().headers(headers).body(user))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(USER_PATH_ID)
    ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable UUID userId) {
        userService.deleteUser(userId, authHeader);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    ResponseEntity<UserResponse> patchUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(userService.patchUser(userRegisterRequest));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserFromToken() {
        return userService.getLoggedUserResponse()
                .map(profile -> ResponseEntity.ok().body(profile))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(USER_PATH_ID + "/posts")
    public Page<PostResponse> listUserPosts(
            @PathVariable UUID userId,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return postService.getPostsByUserId(userId, pageNumber, pageSize);
    }

    @PostMapping(FOLLOW_USER_TOGGLE)
    ResponseEntity<?> toggleFollow(@PathVariable UUID userId) {
        followService.toggleFollow(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/followings")
    Page<FollowResponse> listFollowings(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return followService.listFollowings(pageNumber, pageSize);
    }

    @GetMapping("/followers")
    Page<FollowResponse> listFollowers(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return followService.listFollowers(pageNumber, pageSize);
    }

    @GetMapping(USER_PATH_ID + "/post-likes")
    List<ReactionResponse> getPostLikesByUserId(@PathVariable UUID userId) {
        return reactionService.getReactionsByUserId(userId, ContentType.POST);
    }

    @GetMapping(USER_PATH_ID + "/comment-likes")
    List<ReactionResponse> getCommentLikesByUserId(@PathVariable UUID userId) {
        return reactionService.getReactionsByUserId(userId, ContentType.COMMENT);
    }

}
