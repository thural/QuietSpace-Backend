package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.response.AuthResponse;
import dev.thural.quietspacebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    AuthController(UserService userService){
        this.userService = userService;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    ResponseEntity createUser(@Validated @RequestBody UserDTO user) {
        AuthResponse authResponse = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", UserController.USER_PATH + "/" + authResponse.getResourceId());
        return new ResponseEntity(authResponse, headers, HttpStatus.CREATED);
    }
}
