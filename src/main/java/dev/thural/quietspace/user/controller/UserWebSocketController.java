package dev.thural.quietspace.user.controller;

import dev.thural.quietspace.user.User;
import dev.thural.quietspace.user.UserService;
import dev.thural.quietspace.user.dto.UserResponse;
import dev.thural.quietspace.websocket.model.UserRepresentation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

import static dev.thural.quietspace.shared.enums.StatusType.OFFLINE;
import static dev.thural.quietspace.websocket.constant.WebSocketPaths.ONLINE_USERS;
import static dev.thural.quietspace.websocket.constant.WebSocketPaths.SET_ONLINE_STATUS;
import static dev.thural.quietspace.websocket.constant.WebSocketPaths.USER_PUBLIC;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserWebSocketController {

    private final UserService userService;
    private final SimpMessagingTemplate template;

    @MessageMapping(SET_ONLINE_STATUS)
    @SendTo(USER_PUBLIC)
    public UserRepresentation goOffline(@Payload @Valid UserRepresentation user) {
        userService.setOnlineStatus(user.getEmail(), OFFLINE);
        return user;
    }

    @MessageMapping(ONLINE_USERS)
    public void getOnlineUsers() {
        User signedUser = userService.getSignedUser();
        List<UserResponse> onlineUsers = userService.findConnectedFollowings();
        template.convertAndSendToUser(signedUser.getId().toString(), ONLINE_USERS, onlineUsers);
    }

}
