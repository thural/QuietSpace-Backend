package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.request.LoginRequest;
import dev.thural.quietspace.model.response.AuthResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.AuthService;
import dev.thural.quietspace.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;


    @Override
    public AuthResponse register(UserRequest user) {
        String userPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(userPassword));

        User newUser = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        User savedUser = userRepository.save(userMapper.userRequestToEntity(user));

        Authentication authentication = generateAuthentication(user.getEmail(), userPassword);

        String token = JwtProvider.generateToken(authentication);
        String userId = savedUser.getId().toString();
        return new AuthResponse(UUID.randomUUID(), token, userId, "register success");
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String userEmail = loginRequest.getEmail();
        String userPassword = loginRequest.getPassword();
        Authentication authentication = generateAuthentication(userEmail, userPassword);
        String token;

        if(tokenRepository.existsByEmail(userEmail)){
            token = tokenRepository.getByEmail(userEmail).getJwtToken();
            tokenRepository.deleteByEmail(userEmail);
        } else {
            token = JwtProvider.generateToken(authentication);
        }

        Optional<User> optionalUser = userRepository.findUserEntityByEmail(userEmail);
        User user = optionalUser.orElseThrow(() -> new AuthenticationCredentialsNotFoundException("login failed"));
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
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (userDetails == null)
            throw new BadCredentialsException("invalid username");

        if (!passwordEncoder.matches(password, userDetails.getPassword()))
            throw new BadCredentialsException("invalid password");

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities());
    }

    @Override
    public void addToBlacklist(String authHeader, String email) {
        String token = authHeader.substring(7);
        boolean isBlacklisted = tokenRepository.existsByJwtToken(token);
        if (!isBlacklisted) tokenRepository.save(Token.builder()
                .jwtToken(token)
                .email(email)
                .build()
        );
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean isBlacklisted(String authHeader) {
        String token = authHeader.substring(7);
        return tokenRepository.existsByJwtToken(token);
    }

}