package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.request.LoginRequest;
import dev.thural.quietspace.model.response.AuthResponse;
import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.AuthService;
import dev.thural.quietspace.utils.enums.RoleType;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {


    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;


    @Override
    public AuthResponse register(UserRegisterRequest user) {
        String userPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(userPassword));

        Role userRole = roleRepository.findByName(RoleType.USER.toString())
                // todo - better exception handling
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));

        User savedUser = userRepository.save(
                User.builder()
                        .username(user.getUsername())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .password(user.getPassword())
                        .email(user.getEmail())
                        .roles(List.of(userRole))
                        .accountLocked(false)
                        .enabled(true) // TODO: rollback to false value after testing
                        .build()
        );

        String token = jwtService.generateToken(savedUser);
        String userId = savedUser.getId().toString();
        return new AuthResponse(UUID.randomUUID(), token, userId, "register success");
    }


    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String userEmail = loginRequest.getEmail();
        User user = userRepository.findUserEntityByEmail(userEmail)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("login failed"));

        if (tokenRepository.existsByEmail(userEmail)) tokenRepository.deleteByEmail(userEmail);
        String token = jwtService.generateToken(user);
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
    public Authentication generateAuthentication(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

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
        boolean isBlacklisted = tokenRepository.existsByToken(token);
        if (!isBlacklisted) tokenRepository.save(Token.builder()
                .token(token)
                .email(email)
                .build()
        );
    }

    @Override
    public boolean isBlacklisted(String authHeader) {
        String token = authHeader.substring(7);
        return tokenRepository.existsByToken(token);
    }

}