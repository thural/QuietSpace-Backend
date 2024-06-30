package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.request.LoginRequest;
import dev.thural.quietspace.model.response.AuthResponse;
import dev.thural.quietspace.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    ResponseEntity<?> signupUser(@Validated @RequestBody UserRegisterRequest user) {
        AuthResponse authResponse = authService.register(user);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {

        AuthResponse authResponse = authService.login(loginRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);

    }

    @PostMapping("/logout")
    ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
