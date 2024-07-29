package dev.thural.quietspace.authentication;

import dev.thural.quietspace.authentication.model.AuthRequest;
import dev.thural.quietspace.authentication.model.AuthResponse;
import dev.thural.quietspace.authentication.model.RegistrationRequest;
import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.security.JwtService;
import dev.thural.quietspace.service.impls.EmailService;
import dev.thural.quietspace.utils.enums.EmailTemplateName;
import dev.thural.quietspace.utils.enums.RoleType;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${spring.application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        log.info("Registering new user with email: {}", request.getEmail());

        Role userRole = roleRepository.findByName(RoleType.USER.toString())
                .orElseThrow(() -> new IllegalStateException("ROLE USER has not been initiated"));

        User user = User.builder()
                .username(request.getUsername())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        log.info("user email: {}", user.getEmail());

        User savedUser = userRepository.save(user);
        sendValidationEmail(savedUser);
    }

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

        String jwtToken = jwtService.generateToken(claims, user);
        log.info("generated jwt token during authentication: {}", jwtToken);

        return AuthResponse.builder()
                .id(UUID.randomUUID())
                .message("authentication was successful")
                .userId(user.getId().toString())
                .token(jwtToken)
                .build();
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token existingToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (OffsetDateTime.now().isAfter(existingToken.getExpireDate())) {
            sendValidationEmail(existingToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent");
        }

        User user = userRepository.findById(existingToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        existingToken.setValidateDate(OffsetDateTime.now());
        tokenRepository.save(existingToken);
    }

    public void signout(String authHeader) {
        String currentUserName = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            addToBlacklist(authHeader, currentUserName);
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
        log.info("sending to email address: {}", user.getEmail());
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


    public void addToBlacklist(String authHeader, String email) {
        String token = authHeader.substring(7);
        boolean isBlacklisted = tokenRepository.existsByToken(token);
        if (!isBlacklisted) tokenRepository.save(Token.builder()
                .token(token)
                .email(email)
                .build()
        );
    }
}
