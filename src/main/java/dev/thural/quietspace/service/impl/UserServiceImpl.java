package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import dev.thural.quietspace.enums.StatusType;
import dev.thural.quietspace.exception.CustomErrorException;
import dev.thural.quietspace.exception.UnauthorizedException;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.query.UserQuery;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.PageUtils;
import dev.thural.quietspace.utils.PagingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static dev.thural.quietspace.enums.StatusType.OFFLINE;
import static dev.thural.quietspace.enums.StatusType.ONLINE;
import static dev.thural.quietspace.utils.PagingProvider.DEFAULT_SORT_OPTION;
import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserQuery userQuery;

    @Override
    public Page<UserResponse> listUsers(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return userRepository.findAll(pageRequest).map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> queryUsers(String username, String firstname, String lastname, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        return userQuery.findAllByQuery(username, firstname, lastname, pageRequest)
                .map(userMapper::toResponse);
    }

    @Override
    public Optional<User> getUserById(UUID memberId) {
        return userRepository.findById(memberId);
    }

    @Override
    public Page<UserResponse> listUsersByUsername(String username, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = PagingProvider.buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<User> userPage;

        if (StringUtils.hasText(username)) {
            userPage = userRepository.findAllBySearchTerm(username, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.map(userMapper::toResponse);
    }

    @Override
    public Optional<UserResponse> getLoggedUserResponse() {
        UserResponse userResponse = userMapper.toResponse(getSignedUser());
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
    public Optional<UserResponse> getUserResponseById(UUID userId) {
        User user = checkUserProfileAccessAndReturnUser(userId);
        return Optional.of(user).map(userMapper::toResponse);
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

        BeanUtils.copyProperties(userRegisterRequest, signedUser);
        return userMapper.toResponse(userRepository.save(signedUser));
    }

    private static boolean isHasAdminRole(User signedUser) {
        return signedUser.getRole().name().equals("ROLE_".concat(Role.ADMIN.name()));
    }

    private User checkUserProfileAccessAndReturnUser(UUID userId) {
        User signedUser = getSignedUser();
        if (signedUser.getId().equals(userId)) return signedUser;
        User user = getUserById(userId).orElseThrow(UserNotFoundException::new);
        if (!user.getProfileSettings().getIsPrivateAccount()) return user;
        if (user.getFollowers().contains(getSignedUser())) return user;
        throw new UnauthorizedException("signed user has no access to requested resource");
    }

    @Override
    public Page<UserResponse> listFollowings(UUID userId, Integer pageSize, Integer pageNumber) {
        User user = checkUserProfileAccessAndReturnUser(userId);
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<User> userPage = PageUtils.pageFromList(user.getFollowings(), pageRequest);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> listFollowers(UUID userId, Integer pageNumber, Integer pageSize) {
        User user = checkUserProfileAccessAndReturnUser(userId);
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<User> userPage = PageUtils.pageFromList(user.getFollowers(), pageRequest);
        return userPage.map(userMapper::toResponse);
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
                .ifPresent(storedUser -> storedUser.setStatusType(OFFLINE));
    }

    @Override
    public List<UserResponse> findConnectedFollowings() {
        User signedUser = getSignedUser();
        return signedUser.getFollowings()
                .stream()
                .filter(following -> following.getStatusType().equals(ONLINE))
                .map(userMapper::toResponse).toList();
    }

}