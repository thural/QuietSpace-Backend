package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.TokenRepository;
import dev.thural.quietspace.utils.PagingProvider;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
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
        log.info("current user name {}", SecurityContextHolder.getContext().getAuthentication().getName());

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

        if (!loggedUser.getRole().equals(RoleType.ADMIN.toString()) && !loggedUser.getId().equals(userId))
            throw new UnauthorizedException("user denied access to delete the resource");

        userRepository.deleteById(userId);

        tokenRepository.save(Token.builder()
                .token(token)
                .email(loggedUser.getEmail())
                .build()
        );
    }

    @Override
    public User createUser(UserRegisterRequest userRegisterRequest) {
        return userRepository.save(userMapper.userRequestToEntity(userRegisterRequest));
    }

    @Override
    public UserResponse patchUser(UserRegisterRequest userRegisterRequest) {

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains("ADMIN");
        // TODO: use enum types fot authorities
        User loggedUser = getLoggedUser();

        if (!isAdmin && !userRegisterRequest.getEmail().equals(loggedUser.getEmail()))
            throw new UnauthorizedException("logged user has no access to requested resource");

        if (StringUtils.hasText(userRegisterRequest.getUsername()))
            loggedUser.setUsername(userRegisterRequest.getUsername());
        if (StringUtils.hasText(userRegisterRequest.getEmail()))
            loggedUser.setEmail(userRegisterRequest.getEmail());

        return userMapper.userEntityToResponse(userRepository.save(loggedUser));
    }

}