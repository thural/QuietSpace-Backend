package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.request.LoginRequest;
import dev.thural.quietspace.model.response.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthResponse register(UserRegisterRequest user);

    AuthResponse login(LoginRequest loginRequest);

    void logout(String authHeader);

    Authentication generateAuthentication(String username, String password);

    void addToBlacklist(String authHeader, String email);

    boolean isBlacklisted(String authHeader);

}
