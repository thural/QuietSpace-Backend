package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.response.AuthResponse;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
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

    @RequestMapping(value = USER_PATH, method = RequestMethod.GET)
    Page<UserDTO> listUsers(@RequestParam(required = false) String userName,
                            @RequestParam(required = false) Integer pageNumber,
                            @RequestParam(required = false) Integer pageSize) {
        return userService.listUsers(userName, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.GET)
    UserDTO getUserById(@PathVariable("userId") UUID id) {
        Optional<UserDTO> optionalUser = userService.getById(id);
        UserDTO foundUser = optionalUser.orElseThrow(NotFoundException::new);
        return foundUser;
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.POST)
    ResponseEntity createUser(@Validated @RequestBody UserDTO user) {
        AuthResponse authResponse = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", UserController.USER_PATH + "/" + authResponse.getResourceId());
        return new ResponseEntity(authResponse, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.PUT)
    ResponseEntity putUser(@RequestHeader("Authorization") String jwt, @RequestBody UserDTO user) {
        UserDTO loggedUser = userService.findUserByJwt(jwt).orElse(null);
        assert loggedUser != null;
        userService.updateOne(loggedUser.getId(), user).orElseThrow(NotFoundException::new);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity deleteUser(@PathVariable("userId") UUID id) {
        if (!userService.deleteOne(id)) throw new NotFoundException();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity patchUser(@PathVariable("userId") UUID id, @RequestBody UserDTO user) {
        userService.patchOne(id, user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH + "/search", method = RequestMethod.GET)
    Page<UserDTO> listUsersByQuery(@RequestParam(name = "query") String query,
                                   @RequestParam(required = false) Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize) {
        return userService.listUsersByQuery(query, pageNumber, pageSize);
    }

    @RequestMapping(value = USER_PATH + "/profile", method = RequestMethod.GET)
    public UserDTO getUserFromToken(@RequestHeader("Authorization") String jwt) {
        return userService.findUserByJwt(jwt).orElse(null);
    }

}