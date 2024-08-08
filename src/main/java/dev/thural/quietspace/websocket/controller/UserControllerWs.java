package dev.thural.quietspace.websocket.controller;

import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.utils.enums.StatusType;
import dev.thural.quietspace.websocket.model.UserRepresentation;
import dev.thural.quietspace.websocket.service.UserServiceWs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class UserControllerWs {

    public static final String ONLINE_USERS_PATH = "/user/onlineUsers";

    private final UserServiceWs userService;
    private final SimpMessagingTemplate template;

    // TODO: send to specific list of users instead of broadcasting to all
    @MessageMapping("/user/setOnlineStatus")
    @SendTo("/user/public")
    public UserRepresentation goOffline(@Payload @Valid UserRepresentation user) {
        userService.setOnlineStatus(user.getEmail(), StatusType.OFFLINE);
        return user;
    }

    // TODO: get user from socket session instead of payload
    @MessageMapping(ONLINE_USERS_PATH)
    public void getOnlineUsers(@Payload @Valid UserRepresentation user) {
        List<UserResponse> onLineUsers = userService.findConnectedFollowings(user);
        template.convertAndSendToUser(String.valueOf(user.getEmail()), ONLINE_USERS_PATH, onLineUsers);
    }

}
