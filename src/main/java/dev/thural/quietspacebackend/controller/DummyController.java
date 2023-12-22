package dev.thural.quietspacebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/dummy")
public class DummyController {

    @GetMapping("/users")
    public ResponseEntity<String> sayHelloUsers(){
        return ResponseEntity.ok("hello users");
    }

    @GetMapping("/admins")
    public ResponseEntity<String> sayHelloAdmins(){
        return ResponseEntity.ok("hello admins");
    }

    @GetMapping("/users")
    public ResponseEntity<String> sayHelloManagers(){
        return ResponseEntity.ok("hello managers");
    }



}
