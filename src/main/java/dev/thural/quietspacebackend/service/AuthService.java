package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.model.request.LoginRequest;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponse register(UserDTO userDTO);

    AuthResponse login(LoginRequest loginRequest);

    Authentication authenticate(String email, String password);

    void addToBlacklist(String authHeader);

    boolean isBlacklisted(String authHeader);
}
