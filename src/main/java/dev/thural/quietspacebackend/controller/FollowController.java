package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.FollowDTO;
import dev.thural.quietspacebackend.service.FollowService;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<?> toggleFollow(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("userId") UUID followedUserId) {

        userService.findUserByJwt(authHeader).ifPresent(
                (loggedUser) -> followService.toggleFollow(loggedUser.getId(), followedUserId, authHeader));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = FOLLOW_PATH_ID + "/followings", method = RequestMethod.GET)
    Page<FollowDTO> listFollowings(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("userId") UUID userId,
                                   @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                   @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return followService.listFollowings(userId, authHeader, pageNumber, pageSize);
    }

    @RequestMapping(value = FOLLOW_PATH_ID + "/followers", method = RequestMethod.GET)
    Page<FollowDTO> listFollowers(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable("userId") UUID userId,
                                  @RequestParam(name = "page-number", required = false) Integer pageNumber,
                                  @RequestParam(name = "page-size", required = false) Integer pageSize) {
        return followService.listFollowers(userId, authHeader, pageNumber, pageSize);
    }


}