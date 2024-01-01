package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.service.FollowService;
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
public class FollowController {

    public static final String FOLLOW_PATH = "/api/v1/follows";
    public static final String FOLLOW_PATH_ID = FOLLOW_PATH + "/{userId}";
    public static final String FOLLOW_USER_TOGGLE = FOLLOW_PATH_ID + "/toggleFollow";

    private final UserService userService;
    private final FollowService followService;


    @RequestMapping(value = FOLLOW_USER_TOGGLE, method = RequestMethod.POST)
    ResponseEntity toggleFollow(@RequestHeader("Authorization") String jwt,
                                @PathVariable("userId") UUID followedUserId) {

        userService.findUserByJwt(jwt).ifPresent(
                (loggedUser) -> followService.toggleFollow(loggedUser.getId(), followedUserId, jwt));

        return new ResponseEntity(HttpStatus.OK);
    }

}