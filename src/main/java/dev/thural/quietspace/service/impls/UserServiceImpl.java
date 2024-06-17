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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    public Page<UserResponse> listUsers(String username, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, null);
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

        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, null);
        Page<User> userPage;

        if (StringUtils.hasText(query)) {
            userPage = userRepository.findAllByQuery(query, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::userEntityToResponse);
    }

    @Override
    public Optional<UserResponse> getLoggedUserResponse() {
        UserResponse userResponse = userMapper.userEntityToResponse(getLoggedUser());
        return Optional.of(userResponse);
    }

    @Override
    public User getLoggedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserEntityByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<User> getUsersFromIdList(List<UUID> userIds) {
        return userIds.stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("user not found")))
                .toList();
    }

    @Override
    public Optional<UserResponse> getUserResponseById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        UserResponse userResponse = userMapper.userEntityToResponse(user);
        return Optional.of(userResponse);
    }

    @Override
    public Optional<User> getUserById(UUID userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        return Optional.of(foundUser);
    }

    @Override
    public void deleteUser(UUID userId, String authHeader) {
        String token = authHeader.substring(7);
        User loggedUser = getLoggedUser();

        if (!loggedUser.getRole().equals("admin") && !loggedUser.getId().equals(userId))
            throw new UnauthorizedException("user denied access to delete the resource");

        userRepository.deleteById(userId);

        tokenRepository.save(Token.builder()
                .jwtToken(token)
                .email(loggedUser.getEmail())
                .build()
        );
    }

    @Override
    public UserResponse patchUser(UserRequest userRequest) {

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains("ADMIN");
        // TODO: use enum types fot authorities
        User loggedUser = getLoggedUser();

        if (!isAdmin && !userRequest.getEmail().equals(loggedUser.getEmail()))
            throw new UnauthorizedException("logged user has no access to requested resource");

        if (StringUtils.hasText(userRequest.getUsername()))
            loggedUser.setUsername(userRequest.getUsername());
        if (StringUtils.hasText(userRequest.getEmail()))
            loggedUser.setEmail(userRequest.getEmail());

        return userMapper.userEntityToResponse(userRepository.save(loggedUser));
    }

}