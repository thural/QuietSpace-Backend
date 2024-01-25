package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.model.request.LoginRequest;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.service.TokenBlackList;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final TokenBlackList tokenBlackList;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    ResponseEntity<?> signupUser(@Validated @RequestBody UserDTO user) {
        AuthResponse authResponse = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", UserController.USER_PATH + "/" + authResponse.getUserId());
        return new ResponseEntity<>(authResponse, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userService.getByLoginRequest(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", UserController.USER_PATH + "/" + authResponse.getUserId());
        return new ResponseEntity<>(authResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authHeader) {
        tokenBlackList.addToBlacklist(authHeader);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
