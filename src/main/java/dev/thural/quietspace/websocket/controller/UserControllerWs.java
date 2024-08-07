package dev.thural.quietspace.websocket.controller;

import dev.thural.quietspace.websocket.model.UserRepresentation;
import dev.thural.quietspace.websocket.service.UserServiceWs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserControllerWs {

    private final UserServiceWs userService;

    @MessageMapping("/user/disconnect")
    @SendTo("/user/public")
    public UserRepresentation disconnectUser(@Payload @Valid UserRepresentation user) {
        userService.disconnect(user);
        return user;
    }

}
