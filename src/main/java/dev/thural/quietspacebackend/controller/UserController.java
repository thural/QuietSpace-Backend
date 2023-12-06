package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.User;
import dev.thural.quietspacebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;

    @Autowired
    UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    List<User> getAll(){
        return userRepository.findAll();
    }
}
