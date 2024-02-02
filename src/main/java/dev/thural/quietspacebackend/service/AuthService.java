package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDto;
import dev.thural.quietspacebackend.model.request.LoginRequest;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponse register(UserDto user);

    AuthResponse login(LoginRequest loginRequest);

    void logout(String authHeader);

    Authentication generateAuthentication(String email, String password);

    void addToBlacklist(String authHeader, String email);

    boolean isBlacklisted(String authHeader);
}
