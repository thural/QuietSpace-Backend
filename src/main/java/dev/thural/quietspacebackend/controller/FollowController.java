package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.response.FollowResponse;
import dev.thural.quietspacebackend.service.FollowService;
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

    private final FollowService followService;


    @RequestMapping(value = FOLLOW_USER_TOGGLE, method = RequestMethod.POST)
    ResponseEntity<?> toggleFollow(@PathVariable("userId") UUID followedUserId) {

        followService.toggleFollow(followedUserId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = FOLLOW_PATH + "/followings", method = RequestMethod.GET)
    Page<FollowResponse> listFollowings(@RequestParam(name = "page-number", required = false) Integer pageNumber,
                                        @RequestParam(name = "page-size", required = false) Integer pageSize) {

        return followService.listFollowings(pageNumber, pageSize);
    }

    @RequestMapping(value = FOLLOW_PATH + "/followers", method = RequestMethod.GET)
    Page<FollowResponse> listFollowers(@RequestParam(name = "page-number", required = false) Integer pageNumber,
                                       @RequestParam(name = "page-size", required = false) Integer pageSize) {

        return followService.listFollowers(pageNumber, pageSize);
    }

}