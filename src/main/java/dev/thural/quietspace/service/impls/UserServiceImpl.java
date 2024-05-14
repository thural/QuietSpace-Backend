package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.model.request.UserRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.utils.PagingProvider;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    public Page<UserResponse> listUsers(String username, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = PagingProvider.buildCustomPageRequest(pageNumber, pageSize);
        Page<User> userPage;

        if (StringUtils.hasText(username)) {
            userPage = userRepository.findAllByUsernameIsLikeIgnoreCase("%" + username + "%", pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToResponse);
    }

    @Override
    public Page<UserResponse> listUsersByQuery(String query, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = PagingProvider.buildCustomPageRequest(pageNumber, pageSize);
        Page<User> userPage;

        if (StringUtils.hasText(query)) {
            userPage = userRepository.findAllByQuery(query, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToResponse);
    }

    @Override
    public Optional<UserResponse> findLoggedUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        // TODO: fix jwt expire error
        return Optional.of(userMapper.userEntityToResponse(loggedUser));
    }

    @Override
    public Optional<UserResponse> getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        UserResponse userResponse = userMapper.userEntityToResponse(user);
        return Optional.of(userResponse);
    }

    @Override
    public void deleteUser(UUID userId, String authHeader) {

        boolean isDeleted = false;
        String token = authHeader.substring(7);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (loggedUser.getRole().equals("admin")){
            userRepository.deleteById(userId);
            isDeleted = true;
        } else if (loggedUser.getId().equals(userId)) {
            userRepository.deleteById(userId);
            isDeleted =  true;
        }

        if (isDeleted) tokenRepository.save(Token.builder()
                .jwtToken(token)
                .email(loggedUser.getEmail())
                .build()
        );

    }

    @Override
    public void patchUser(UserRequest userRequest) {

        User requestedUser = userRepository.findUserEntityByEmail(userRequest.getEmail())
                .orElseThrow(UserNotFoundException::new);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains("ADMIN");
        // TODO: use enum types fot authorities
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (!isAdmin && !requestedUser.getEmail().equals(loggedUser.getEmail()))
            throw new UnauthorizedException("logged user has no access to requested resource");

        if (StringUtils.hasText(userRequest.getUsername()))
            loggedUser.setUsername(userRequest.getUsername());
        if (StringUtils.hasText(userRequest.getEmail()))
            loggedUser.setEmail(userRequest.getEmail());
        if (StringUtils.hasText(userRequest.getPassword()))
            loggedUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        userRepository.save(loggedUser);
    }

}