package dev.thural.quietspace.authentication.service;

import dev.thural.quietspace.authentication.model.AuthRequest;
import dev.thural.quietspace.authentication.model.AuthResponse;
import dev.thural.quietspace.authentication.model.RegistrationRequest;
import dev.thural.quietspace.entity.ProfileSettings;
import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.EmailTemplateName;
import dev.thural.quietspace.enums.StatusType;
import dev.thural.quietspace.exception.ActivationTokenException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.service.impl.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HashMap;

import static dev.thural.quietspace.enums.Role.USER;
import static dev.thural.quietspace.enums.StatusType.OFFLINE;
import static dev.thural.quietspace.enums.StatusType.ONLINE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${spring.application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        log.info("registering new user with email: {}", request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .role(USER)
                .build();

        ProfileSettings settings = new ProfileSettings(user);
        user.setProfileSettings(settings);

        User savedUser = userRepository.save(user);
        sendValidationEmail(savedUser);
    }

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        log.info("authenticating user by email: {}", request.getEmail());
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        User user = ((User) auth.getPrincipal());
        claims.put("fullName", user.getFullName());

        String jwtAccessToken = jwtService.generateToken(claims, user);
        String jwtRefreshToken = jwtService.generateRefreshToken(claims, user);
        log.info("generated jwt token during authentication: {}", jwtAccessToken);

        setOnlineStatus(user.getEmail(), ONLINE);
        return AuthResponse.builder()
                .message("authentication was successful")
                .userId(user.getId().toString())
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token existingToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ActivationTokenException("invalid token, please try again"));

        if (OffsetDateTime.now().isAfter(existingToken.getExpireDate())) {
            sendValidationEmail(existingToken.getUser());
            throw new RuntimeException("activation token has expired... a new token has been sent");
        }

        User user = userRepository.findById(existingToken.getUser().getId()).orElseThrow(UserNotFoundException::new);
        user.setEnabled(true);
        existingToken.setValidateDate(OffsetDateTime.now());
    }

    @Transactional
    public void signout(String authHeader) {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("username in security context on signing out: {}", currentUserName);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            addToBlacklist(authHeader, currentUserName);
            setOnlineStatus(OFFLINE);
            SecurityContextHolder.clearContext();
        }
    }

    private String generateAndSaveActivationToken(User user) {
        String activationCode = generateActivationCode(6);

        Token token = Token.builder()
                .token(activationCode)
                .email(user.getEmail())
                .expireDate(OffsetDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokenRepository.save(token);
        return activationCode;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        log.info("sending activation code to email address: {}", user.getEmail());
        String newActivationCode = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newActivationCode,
                "account activation"
        );
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder generatedCode = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            generatedCode.append(characters.charAt(randomIndex));
        }

        log.info("generated activation token: {}", generatedCode.toString());
        return generatedCode.toString();
    }

    public void addToBlacklist(String authHeader, String username) {
        String jwtToken = authHeader.substring(7);
        boolean isBlacklisted = tokenRepository.existsByToken(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        if (!isBlacklisted) saveToken(jwtToken, user);
    }

    private void saveToken(String jwtToken, User user) {
        tokenRepository.save(Token.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .user(user)
                .build());
    }

    public AuthResponse refreshToken(String authHeader) {
        String refreshToken = authHeader.substring(7);

        AuthResponse authResponse = AuthResponse.builder()
                .refreshToken(refreshToken)
                .message("token refresh was failed")
                .build();

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) return authResponse;
        if (tokenRepository.existsByToken(refreshToken)) return authResponse;

        String username = jwtService.extractUsername(refreshToken);
        log.info("extracted username during jwt filtering: {}", username);
        if (username == null) return authResponse;

        User user = userRepository.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        if (!jwtService.isTokenValid(refreshToken, user)) return authResponse;

        var claims = new HashMap<String, Object>();
        claims.put("fullName", user.getFullName());
        String newAccessToken = jwtService.generateToken(claims, user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .message("token was refreshed")
                .userId(String.valueOf(user.getId()))
                .build();
    }

    public void resendActivationToken(String email) throws MessagingException {
        User foundUser = userRepository
                .findUserEntityByEmail(email).orElseThrow(UserNotFoundException::new);
        if (foundUser.isEnabled())
            throw new ActivationTokenException("invalid request: account has already been activated");
        sendValidationEmail(foundUser);
    }

    private void setOnlineStatus(StatusType type) {
        // TODO: consider user settings after implementation
        User user = userService.getSignedUser();
        user.setStatusType(type);
    }

    private void setOnlineStatus(String email, StatusType type) {
        // TODO: consider user settings after implementation
        userRepository.findUserEntityByEmail(email)
                .ifPresent(user -> user.setStatusType(type));
    }

}
