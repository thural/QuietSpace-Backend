package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.TokenEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.UserDto;
import dev.thural.quietspacebackend.model.request.LoginRequest;
import dev.thural.quietspacebackend.model.response.AuthResponse;
import dev.thural.quietspacebackend.repository.TokenRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.AuthService;
import dev.thural.quietspacebackend.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;


    @Override
    public AuthResponse register(UserDto userDTO) {
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserEntity savedUser = userRepository.save(userMapper.userDtoToEntity(userDTO));

        Authentication authentication = authenticate(userDTO.getEmail(), userDTO.getPassword());

//        authManager.authenticate(authentication);

        String token = JwtProvider.generateToken(authentication);
        String userId = savedUser.getId().toString();
        return new AuthResponse(token, userId, "register success");
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        String token = JwtProvider.generateToken(authentication);

//        authManager.authenticate(authentication);

        tokenRepository.deleteByEmail(loginRequest.getEmail());

        Optional<UserEntity> optionalUser = userRepository.findUserEntityByEmail(loginRequest.getEmail());
        String userId = optionalUser.isPresent() ? optionalUser.get().getId().toString() : "null";

        return new AuthResponse(token, userId, "login success");
    }

    @Override
    public void logout(String authHeader) {
        String currentUserName = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        System.out.println("logged User name: " + currentUserName);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            addToBlacklist(authHeader, currentUserName);
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public Authentication authenticate(String email, String password) {
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
        if (!isBlacklisted) tokenRepository.save(TokenEntity.builder()
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