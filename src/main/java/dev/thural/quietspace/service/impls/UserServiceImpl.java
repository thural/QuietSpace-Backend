package dev.thural.quietspace.service.impls;

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
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
        return userRepository.findUserByUsername(email).orElseThrow(UserNotFoundException::new);
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
    public void deleteUserById(UUID userId) {
        User loggedUser = getLoggedUser();
        boolean hasAdminRole = loggedUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleType.ADMIN.name()));

        if (!hasAdminRole && !loggedUser.getId().equals(userId))
            throw new UnauthorizedException("user denied access to delete the resource");

        userRepository.deleteById(userId);
    }

    @Override
    public User createUser(UserRegisterRequest userRegisterRequest) {
        return userRepository.save(userMapper.userRequestToEntity(userRegisterRequest));
    }

    @Override
    public UserResponse patchUser(UserRegisterRequest userRegisterRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("granted authorities in patchUser: {}", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce(" ", String::concat));

        boolean hasAdminRole = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_".concat(RoleType.ADMIN.toString())));

        User loggedUser = getLoggedUser();
        if (!hasAdminRole && !userRegisterRequest.getEmail().equals(loggedUser.getEmail()))
            throw new UnauthorizedException("logged user has no access to requested resource");

        BeanUtils.copyProperties(loggedUser, userRegisterRequest);
        return userMapper.userEntityToResponse(userRepository.save(loggedUser));
    }

}