package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.CommentLikeDto;
import dev.thural.quietspacebackend.model.PostDto;
import dev.thural.quietspacebackend.model.PostLikeDto;
import dev.thural.quietspacebackend.model.UserDto;
import dev.thural.quietspacebackend.service.*;
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
public class UserController {

    public static final String USER_PATH = "/api/v1/users";
    public static final String USER_PATH_ID = USER_PATH + "/{userId}";

    private final UserService userService;
    private final PostService postService;
    private final AuthService authService;
    private final CommentService commentService;

    @RequestMapping(value = USER_PATH, method = RequestMethod.GET)
    Page<UserDto> listUsers(@RequestParam(name = "username", required = false) String username,
                            @RequestParam(name = "page-number", required = false) Integer pageNumber,
                            @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return userService.listUsers(username, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH + "/search", method = RequestMethod.GET)
    Page<UserDto> listUsersByQuery(@RequestParam(name = "query", required = false) String query,
                                   @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                   @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return userService.listUsersByQuery(query, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.GET)
    UserDto getUserById(@PathVariable("userId") UUID userId) {
        Optional<UserDto> optionalUser = userService.getUserById(userId);
        return optionalUser.orElse(null);
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.PUT)
    ResponseEntity<?> putUser(@RequestHeader("Authorization") String authHeader,
                              @RequestBody @Validated UserDto user) {
        userService.updateUser(authHeader, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable("userId") UUID id) {
        userService.deleteUser(id, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.PATCH)
    ResponseEntity<?> patchUser(@RequestHeader("Authorization") String authHeader,
                                @RequestBody UserDto userDto) {
        userService.patchUser(userDto, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH + "/profile", method = RequestMethod.GET)
    public UserDto getUserFromToken(@RequestHeader("Authorization") String authHeader) {
        return userService.findUserDtoByJwt(authHeader).orElse(null);
    }

    @RequestMapping(value = USER_PATH_ID + "/posts", method = RequestMethod.GET)
    public Page<PostDto> listUserPosts(@PathVariable("userId") UUID userId,
                                       @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                       @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getPostsByUserId(userId, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH_ID + "/post-likes", method = RequestMethod.GET)
    List<PostLikeDto> getAllPostLikesByUserId(@PathVariable("userId") UUID userId) {
        return postService.getPostLikesByUserId(userId);
    }

    @RequestMapping(value = USER_PATH_ID + "/comment-likes", method = RequestMethod.GET)
    List<CommentLikeDto> getAllCommentLikesByUserId(@PathVariable("userId") UUID userId) {
        return commentService.getAllByUserId(userId);
    }

}