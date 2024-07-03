package dev.thural.quietspace.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String hello() {
        return "Hello Backend";
    }

    @GetMapping("/get-remote-host")
    public String getRemoteHost(HttpServletRequest request) {
        return "Remote Host: " + request.getRemoteAddr();
    }

}
