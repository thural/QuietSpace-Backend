package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.request.LoginRequest;
import dev.thural.quietspace.model.response.AuthResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Override
    public AuthResponse register(UserRequest userRequest) {

        String userPassword = userRequest.getPassword();
        userRequest.setPassword(passwordEncoder.encode(userPassword));
        User savedUser = userRepository.save(userMapper.userRequestToEntity(userRequest));

        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRequest.getEmail(), userPassword)
        );
        var claims = new HashMap<String, Object>();
        var user = (User) auth.getPrincipal();
        claims.put("fullName", user.getFullName());
        String token = jwtService.generateToken(claims, user);

        String userId = savedUser.getId().toString();
        return new AuthResponse(UUID.randomUUID(), token, userId, "register success");

    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String userEmail = loginRequest.getEmail();
        String userPassword = loginRequest.getPassword();


        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, userPassword)
        );
        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName", user.getFullName());
        String token = jwtService.generateToken(claims, user);

        Optional<Token> existingToken = tokenRepository.getByEmail(userEmail);
        if (existingToken.isPresent()) {
            if (jwtService.isTokenValid(existingToken.get().getToken(), user))
                token = existingToken.get().getToken();
            tokenRepository.deleteByEmail(userEmail);
        }

        return new AuthResponse(UUID.randomUUID(), token, user.getId().toString(), "login success");
    }

    @Override
    public void logout(String authHeader) {
        String currentUserName = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            addToBlacklist(authHeader, currentUserName);
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public Authentication generateAuthentication(String email, String password) {
        return new UsernamePasswordAuthenticationToken(email, password);
    }

    @Override
    public void addToBlacklist(String authHeader, String email) {
        String token = authHeader.substring(7);
        boolean isBlacklisted = tokenRepository.existsByToken(token);
        if (!isBlacklisted) tokenRepository.save(Token.builder()
                .token(token)
                .email(email)
                .build()
        );
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean isBlacklisted(String authHeader) {
        String token = authHeader.substring(7);
        return tokenRepository.existsByToken(token);
    }

}