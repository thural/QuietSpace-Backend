package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.enums.NotificationType;
import dev.thural.quietspace.utils.enums.StatusType;
import dev.thural.quietspace.websocket.model.UserRepresentation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    public static final String USER_PATH = "/api/v1/users";
    public static final String USER_PATH_ID = "/{userId}";
    public static final String FOLLOW_PATH_ID = "/follow/{userId}";
    public static final String ONLINE_USERS_PATH = "/user/onlineUsers";
    public static final String FOLLOW_USER_TOGGLE_PATH = FOLLOW_PATH_ID + "/toggle-follow";

    private final UserService userService;
    private final SimpMessagingTemplate template;
    private final PostService postService;
    private final NotificationService notificationService;

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
    ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        userService.deleteUserById(userId);
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

    @PostMapping(FOLLOW_USER_TOGGLE_PATH)
    ResponseEntity<?> toggleFollow(@PathVariable UUID userId) {
        userService.toggleFollow(userId);
        notificationService.processNotification(NotificationType.FOLLOW_REQUEST, userId);
        log.info("toggle follow for userId: {}", userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/followers/remove/{userId}")
    ResponseEntity<?> removeFollower(@PathVariable UUID userId) {
        userService.removeFollower(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/followings")
    Page<UserResponse> listFollowings(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return userService.listFollowings(pageNumber, pageSize);
    }

    @GetMapping("/followers")
    Page<UserResponse> listFollowers(
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return userService.listFollowers(pageNumber, pageSize);
    }


    // TODO: send to specific list of users instead of broadcasting to all
    @MessageMapping("/user/setOnlineStatus")
    @SendTo("/user/public")
    public UserRepresentation goOffline(@Payload @Valid UserRepresentation user) {
        userService.setOnlineStatus(user.getEmail(), StatusType.OFFLINE);
        return user;
    }

    // TODO: get user from socket session instead of payload
    @MessageMapping(ONLINE_USERS_PATH)
    public void getOnlineUsers(@Payload @Valid UserRepresentation user) {
        List<UserResponse> onLineUsers = userService.findConnectedFollowings(user);
        template.convertAndSendToUser(String.valueOf(user.getEmail()), ONLINE_USERS_PATH, onLineUsers);
    }

}
