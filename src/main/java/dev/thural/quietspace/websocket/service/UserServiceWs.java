package dev.thural.quietspace.websocket.service;

import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.utils.enums.StatusType;
import dev.thural.quietspace.websocket.model.UserRepresentation;

import java.util.List;

public interface UserServiceWs {

    void setOnlineStatus(String userEmail, StatusType type);

    List<UserResponse> findConnectedFollowings(UserRepresentation user);
}
