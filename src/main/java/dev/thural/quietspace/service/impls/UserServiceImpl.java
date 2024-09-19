package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import dev.thural.quietspace.enums.StatusType;
import dev.thural.quietspace.exception.CustomErrorException;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.ListToPage;
import dev.thural.quietspace.utils.PagingProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.DEFAULT_SORT_OPTION;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public Page<UserResponse> listUsers(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<User> userPage = userRepository.findAll(pageRequest);
        return userPage.map(userMapper::userEntityToResponse);
    }

    @Override
    public Page<UserResponse> listUsersByQuery(String query, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
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
        UserResponse userResponse = userMapper.userEntityToResponse(getSignedUser());
        return Optional.of(userResponse);
    }

    @Override
    public User getSignedUser() {
        log.info("current user name in Spring SecurityContext {}", SecurityContextHolder.getContext().getAuthentication().getName());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<User> getUsersFromIdList(List<UUID> userIds) {
        return userIds.stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(UserNotFoundException::new))
                .toList();
    }

    @Override
    public Optional<UserResponse> getUserResponseById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        UserResponse userResponse = userMapper.userEntityToResponse(user);
        return Optional.of(userResponse);
    }

    @Override
    public Optional<User> getUserById(UUID userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return Optional.of(foundUser);
    }

    @Override
    public void deleteUserById(UUID userId) {
        User signedUser = getSignedUser();
        boolean hasAdminRole = isHasAdminRole(signedUser);

        if (!hasAdminRole && !signedUser.getId().equals(userId))
            throw new UnauthorizedException("user denied access to delete the resource");

        userRepository.deleteById(userId);
    }

    @Override
    public UserResponse patchUser(UserRegisterRequest userRegisterRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("granted authorities in patchUser: {}", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce(" ", String::concat));

        User signedUser = getSignedUser();
        boolean hasAdminRole = isHasAdminRole(signedUser);

        if (!hasAdminRole && !userRegisterRequest.getEmail().equals(signedUser.getEmail()))
            throw new UnauthorizedException("signed user has no access to requested resource");

        BeanUtils.copyProperties(signedUser, userRegisterRequest);
        return userMapper.userEntityToResponse(userRepository.save(signedUser));
    }

    private static boolean isHasAdminRole(User signedUser) {
        return signedUser.getRole().name().equals("ROLE_".concat(Role.ADMIN.name()));
    }

    @Override
    public Page<UserResponse> listFollowings(Integer pageNumber, Integer pageSize) {
        User user = getSignedUser();
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<User> userPage = new ListToPage<User>().convert(user.getFollowings(), pageRequest);
        return userPage.map(userMapper::userEntityToResponse);
    }

    @Override
    public Page<UserResponse> listFollowers(Integer pageNumber, Integer pageSize) {
        User user = getSignedUser();
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<User> userPage = new ListToPage<User>().convert(user.getFollowers(), pageRequest);
        return userPage.map(userMapper::userEntityToResponse);
    }

    @Override
    @Transactional
    public void toggleFollow(UUID followedUserId) {
        User user = getSignedUser();
        if (user.getId().equals(followedUserId))
            throw new CustomErrorException(HttpStatus.BAD_REQUEST,
                    "users can't unfollow themselves");
        User followedUser = userRepository.findById(followedUserId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getFollowings().contains(followedUser)) {
            user.getFollowings().remove(followedUser);
            followedUser.getFollowers().remove(user);
        } else {
            user.getFollowings().add(followedUser);
            followedUser.getFollowers().add(user);
        }
    }

    @Override
    @Transactional
    public void removeFollower(UUID followingUserId) {
        User user = getSignedUser();
        if (user.getId().equals(followingUserId))
            throw new CustomErrorException(HttpStatus.BAD_REQUEST,
                    "users can't unfollow themselves");
        User followingUser = userRepository.findById(followingUserId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getFollowers().contains(followingUser)) {
            user.getFollowers().remove(followingUser);
            followingUser.getFollowings().remove(user);
        } else throw new CustomErrorException("user is not found in followers");
    }

    @Override
    public void setOnlineStatus(String userEmail, StatusType type) {
        userRepository.findUserEntityByEmail(userEmail)
                .ifPresent((storedUser) -> {
                    storedUser.setStatusType(StatusType.OFFLINE);
                    userRepository.save(storedUser);
                });
    }

    @Override
    public List<UserResponse> findConnectedFollowings() {
        User signedUser = getSignedUser();
        return signedUser.getFollowings()
                .stream()
                .filter(following -> following.getStatusType().equals(StatusType.ONLINE))
                .map(userMapper::userEntityToResponse).toList();
    }

}