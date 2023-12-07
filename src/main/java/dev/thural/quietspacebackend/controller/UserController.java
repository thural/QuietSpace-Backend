package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping("/{userId}")
    User getUserById(@PathVariable("userId") ObjectId id){
        Optional<User> optionalUser = userService.getById(id);
        User foundUser = optionalUser.orElse(null);
        return foundUser;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    User createUser(@RequestBody User user) {
        return userService.addOne(user);
    }
}