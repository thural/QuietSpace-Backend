package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Follow;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.FollowMapper;
import dev.thural.quietspace.model.response.FollowResponse;
import dev.thural.quietspace.repository.FollowRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowMapper followMapper;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private FollowServiceImpl followService;

    private Follow follow;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        this.user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .email("user1@email.com")
                .role("user")
                .password("pAsSword")
                .build();

        this.user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .email("user2@email.com")
                .role("user")
                .password("pAsSWord")
                .build();

        this.follow = Follow.builder()
                .id(UUID.randomUUID())
                .follower(user1)
                .following(user2)
                .build();
    }

    @Test
    void listFollowings() {
        PageRequest pageRequest = buildPageRequest(1, 50, null);
        when(userService.getLoggedUser()).thenReturn(user1);
        when(followRepository.findAllByFollowingId(user1.getId(), pageRequest)).thenReturn(new PageImpl<>(List.of(follow)));
        when(followMapper.followEntityToResponse(any(Follow.class))).thenReturn(FollowResponse.builder().build());

        Page<FollowResponse> follofingsPage = followService.listFollowings(1, 50);
        assertThat(follofingsPage.getContent()).hasSize(1);
        verify(followRepository, times(1)).findAllByFollowingId(user1.getId(), pageRequest);
    }

    @Test
    void listFollowers() {
        PageRequest pageRequest = buildPageRequest(1, 50, null);
        when(followRepository.findAllByFollowerId(user2.getId(), pageRequest)).thenReturn(new PageImpl<>(List.of(follow)));
        when(followMapper.followEntityToResponse(any(Follow.class))).thenReturn(FollowResponse.builder().build());
        when(userService.getLoggedUser()).thenReturn(user2);

        Page<FollowResponse> followersPage = followService.listFollowers(1, 50);
        assertThat(followersPage.getContent()).hasSize(1);
    }

    @Test
    void toggleFollowExisting() {
        when(userService.getLoggedUser()).thenReturn(user1);
        when(followRepository.existsByFollowerIdAndFollowingId(user1.getId(), user2.getId())).thenReturn(true);

        followService.toggleFollow(user2.getId());

        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(user1.getId(), user2.getId());
        verify(followRepository, times(1)).deleteByFollowerIdAndFollowingId(user1.getId(), user2.getId());
    }

    @Test
    void toggleFollowNew() {
        when(userService.getLoggedUser()).thenReturn(user1);
        when(followRepository.existsByFollowerIdAndFollowingId(user1.getId(), user2.getId())).thenReturn(false);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        followService.toggleFollow(user2.getId());

        verify(followRepository, times(1)).existsByFollowerIdAndFollowingId(user1.getId(), user2.getId());
        verify(followRepository, times(1)).save(any(Follow.class));
    }
}