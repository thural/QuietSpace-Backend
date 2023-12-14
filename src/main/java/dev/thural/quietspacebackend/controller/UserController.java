package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UserController {

    public static final String USER_PATH = "/api/v1/users";
    public static final String USER_PATH_ID = USER_PATH + "/{userId}";

    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.GET)
    List<UserDTO> getAllUsers() {
        return userService.getAll();
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.GET)
    UserDTO getUserById(@PathVariable("userId") UUID id) {
        Optional<UserDTO> optionalUser = userService.getById(id);
        UserDTO foundUser = optionalUser.orElseThrow(NotFoundException::new);
        return foundUser;
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.POST)
    ResponseEntity createUser(@RequestBody UserDTO user) {
        UserDTO savedUser = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", USER_PATH + "/" + savedUser.getId());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity putUser(@PathVariable("userId") UUID id, @RequestBody UserDTO user) {
        userService.updateOne(id, user).orElseThrow(NotFoundException::new);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity deleteUser(@PathVariable("userId") UUID id) {
        userService.deleteOne(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity patchUser(@PathVariable("userId") UUID id, @RequestBody UserDTO user){
        userService.patchOne(id, user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}