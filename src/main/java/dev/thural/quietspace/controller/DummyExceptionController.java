package dev.thural.quietspace.controller;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.CustomDataNotFoundException;
import dev.thural.quietspace.exception.CustomErrorException;
import dev.thural.quietspace.exception.CustomParameterConstraintException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DummyExceptionController {

    private final UserRepository userRepository;

    @GetMapping
    public String home() {
        return "hello jwt";
    }

    @GetMapping("test-custom-data-not-found-exception")
    public ResponseEntity<Void> test1() {
        try {
            // simulating a NullPointerException error
            throw new NullPointerException("Data not found");
        } catch (NullPointerException e) {
            throw new CustomDataNotFoundException(e.getMessage());
        }
    }

    @GetMapping("test-custom-parameter-constraint-exception")
    public ResponseEntity<Void> test2(@RequestParam("value") int value) {
        if (value < 0 || value > 10) {
            throw new CustomParameterConstraintException("value must be >= 0 and <= 10");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("test-custom-error-exception")
    public ResponseEntity<Void> test3() {
        // simulating a CustomDataNotFoundException error
        throw new CustomErrorException(HttpStatus.BAD_REQUEST, "Parameters not passed");
    }

    @GetMapping("test-generic-exception")
    public ResponseEntity<Void> test4() {
        // simulating a generic error
        throw new RuntimeException("Generic Exception");
    }

    @GetMapping("admin-page")
    public ResponseEntity<?> adminAccessTest() {

        return new ResponseEntity<>("access has been granted", HttpStatus.OK);
    }

    @GetMapping("get-user/{username}")
    public ResponseEntity<?> getUserByName(@PathVariable String username) {

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
