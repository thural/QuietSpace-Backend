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
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<User> getAllUsers() {
        return userService.getAll();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    User getUserById(@PathVariable("userId") ObjectId id){
        Optional<User> optionalUser = userService.getById(id);
        User foundUser = optionalUser.orElse(null);
        return foundUser;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    ResponseEntity createUser(@RequestBody User user) {
        User savedUser = userService.addOne(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/users" + "/" + savedUser.getId());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    ResponseEntity putUser(@PathVariable("userId") ObjectId id, @RequestBody User user){
        userService.updateOne(id, user);
        return  new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}