package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<User> getAll() {
        return userService.getAll();
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    User createUser(@RequestBody User user) {
        return userService.addOne(user);
    }
}