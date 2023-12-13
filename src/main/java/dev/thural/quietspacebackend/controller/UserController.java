package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    List<User> getAllUsers() {
        return userService.getAll();
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.GET)
    User getUserById(@PathVariable("userId") ObjectId id) {
        Optional<User> optionalUser = userService.getById(id);
        User foundUser = optionalUser.orElse(null);
        return foundUser;
    }

    @RequestMapping(value = USER_PATH, method = RequestMethod.POST)
    ResponseEntity createUser(@RequestBody User user) {
        User savedUser = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", USER_PATH + "/" + savedUser.getId());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.PUT)
    ResponseEntity putUser(@PathVariable("userId") ObjectId id, @RequestBody User user) {
        userService.updateOne(id, user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.DELETE)
    ResponseEntity deleteUser(@PathVariable("userId") ObjectId id) {
        userService.deleteOne(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = USER_PATH_ID, method = RequestMethod.PATCH)
    ResponseEntity patchUser(@PathVariable("userId") ObjectId id, @RequestBody User user){
        userService.patchOne(id, user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}