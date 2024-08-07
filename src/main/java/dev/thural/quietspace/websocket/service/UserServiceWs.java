package dev.thural.quietspace.websocket.service;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.websocket.model.UserRepresentation;

import java.util.List;

public interface UserServiceWs {

    void disconnect(UserRepresentation user);

    List<UserResponse> findConnectedFollowings(User user);
}
