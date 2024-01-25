package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.service.PostService;
import dev.thural.quietspacebackend.service.TokenBlackList;
import dev.thural.quietspacebackend.service.UserService;
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
public class UserController {

    public static final String USER_PATH = "/api/v1/users";
    public static final String USER_PATH_ID = USER_PATH + "/{userId}";

    private final UserService userService;
    private final PostService postService;
    private final TokenBlackList tokenBlackList;

    @RequestMapping(value = USER_PATH, method = RequestMethod.GET)
    Page<UserDTO> listUsers(@RequestParam(name = "username", required = false) String username,
                            @RequestParam(name = "page-number", required = false) Integer pageNumber,
                            @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return userService.listUsers(username, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH + "/search", method = RequestMethod.GET)
    Page<UserDTO> listUsersByQuery(@RequestParam(name = "query", required = false) String query,
                                   @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                   @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return userService.listUsersByQuery(query, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.GET)
    UserDTO getUserById(@PathVariable("userId") UUID userId) {
        Optional<UserDTO> optionalUser = userService.getById(userId);
        return optionalUser.orElse(null);
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.POST)
    ResponseEntity<?> createUser(@Validated @RequestBody UserDTO user) {
        AuthResponse authResponse = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", UserController.USER_PATH + "/" + authResponse.getUserId());
        return new ResponseEntity<>(authResponse, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.PUT)
    ResponseEntity<?> putUser(@RequestHeader("Authorization") String jwt, @RequestBody @Validated UserDTO userDTO) {
        userService.findUserByJwt(jwt).ifPresent(
                (loggedUser) -> userService.updateOne(loggedUser, userDTO));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String jwt, @PathVariable("userId") UUID id) {
        boolean isDeleted = userService.deleteOne(id, jwt);
        if (isDeleted) tokenBlackList.addToBlacklist(jwt);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.PATCH)
    ResponseEntity<?> patchUser(@RequestHeader("Authorization") String jwt,
                                @RequestBody UserDTO userDTO) {
        userService.patchOne(userDTO, jwt);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH + "/profile", method = RequestMethod.GET)
    public UserDTO getUserFromToken(@RequestHeader("Authorization") String jwt) {
        return userService.findUserDtoByJwt(jwt).orElse(null);
    }

    @RequestMapping(value = USER_PATH_ID + "/posts", method = RequestMethod.GET)
    public Page<PostDTO> listUserPosts(@PathVariable("userId") UUID userId,
                                       @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                       @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return postService.getPostsByUserId(userId, pageNumber, pageSize);
    }

}