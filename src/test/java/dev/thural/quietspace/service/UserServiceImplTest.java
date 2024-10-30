package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.enums.Role;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.UserRegisterRequest;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.impl.UserServiceImpl;
import dev.thural.quietspace.utils.PagingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    UserServiceImpl userService;

    private UUID userId;
    private User user;
    private UserRegisterRequest registerRequest;

    @BeforeEach
    void initMockData() {
        this.userId = UUID.fromString("e18d0c0c-37a4-4e50-8041-bd49ffde8182");

        this.user = User.builder()
                .id(userId)
                .username("user")
                .email("user@email.com")
                .password("pAsSword")
                .role(Role.USER)
                .build();

        this.registerRequest = UserRegisterRequest.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .password(user.getPassword())
                .build();
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(userId).orElseThrow(null);

        assertThat(foundUser).isInstanceOf(User.class);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserResponseById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse foundUser = userService.getUserResponseById(userId).orElseThrow(null);

        assertThat(foundUser).isInstanceOf(UserResponse.class);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testListAllTest() {
        PageRequest pageRequest = PagingProvider.buildPageRequest(1, 50, null);
        when(userRepository.findAll(pageRequest)).thenReturn(Page.empty());

        Page<UserResponse> userPage = userService.listUsers(1, 50);

        assertThat(userPage).isEmpty();
        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void testGetUsersFromIdList() {
        List<UUID> userIdList = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        List<User> userList = userService.getUsersFromIdList(userIdList);

        assertThat(userList).isNotEmpty();
        verify(userRepository, times(2)).findById(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testGetSignedUser() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findUserByUsername(any())).thenReturn(Optional.of(user));

        User loggedUser = userService.getSignedUser();

        assertThat(loggedUser).isInstanceOf(User.class);
        verify(userRepository, times(1)).findUserByUsername(any());
    }

    @Test
    void testGetSignedUserResponse() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findUserByUsername(any())).thenReturn(Optional.of(user));

        UserResponse loggedUser = userService.getLoggedUserResponse().orElse(null);

        assertThat(loggedUser).isInstanceOf(UserResponse.class);
        verify(userRepository, times(1)).findUserByUsername(any());
    }

    @Test
    void testDeleteUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findUserByUsername(any())).thenReturn(Optional.of(user));
        SecurityContextHolder.setContext(securityContext);

        userService.deleteUserById(userId);

        verify(userRepository, times(1)).findUserByUsername(any());
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testUpdateUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findUserByUsername(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        UserResponse userResponse = userService.patchUser(registerRequest);

        assertThat(userResponse).isInstanceOf(UserResponse.class);
        verify(userRepository, times(1)).findUserByUsername(any());
        verify(userRepository, times(1)).save(any(User.class));
    }


}