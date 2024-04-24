package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.Token;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.model.response.UserResponse;
import dev.thural.quietspacebackend.repository.TokenRepository;
import dev.thural.quietspacebackend.utils.PagingProvider;
import dev.thural.quietspacebackend.entity.User;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
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
    public Boolean deleteUser(UUID userId, String authHeader) {

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

        return isDeleted;
    }

    @Override
    public void patchUser(UserResponse userResponse) {

        User requestedUser = userRepository.findUserEntityByEmail(userResponse.getEmail())
                .orElseThrow(UserNotFoundException::new);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains("ADMIN");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (!isAdmin && !requestedUser.getEmail().equals(loggedUser.getEmail()))
            throw new AccessDeniedException("logged user has no access to requested resource");

        if (StringUtils.hasText(userResponse.getUsername()))
            loggedUser.setUsername(userResponse.getUsername());

        if (StringUtils.hasText(userResponse.getEmail()))
            loggedUser.setEmail(userResponse.getEmail());

        if (StringUtils.hasText(userResponse.getPassword()))
            loggedUser.setPassword(passwordEncoder.encode(userResponse.getPassword()));

        userRepository.save(loggedUser);
    }

}