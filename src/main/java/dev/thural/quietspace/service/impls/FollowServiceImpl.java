package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Follow;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.FollowMapper;
import dev.thural.quietspace.model.response.FollowResponse;
import dev.thural.quietspace.repository.FollowRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    private User getUserFromSecurityContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }

    @Override
    public Page<FollowResponse> listFollowings(Integer pageNumber, Integer pageSize) {
        User user = getUserFromSecurityContext();

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        Page<Follow> userPage = followRepository.findAllByFollowingId(user.getId(), pageRequest);

        return userPage.map(followMapper::followEntityToResponse);
    }

    @Override
    public Page<FollowResponse> listFollowers(Integer pageNumber, Integer pageSize) {
        User user = getUserFromSecurityContext();

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        Page<Follow> userPage = followRepository.findAllByFollowerId(user.getId(), pageRequest);

        return userPage.map(followMapper::followEntityToResponse);
    }

    @Override
    public void toggleFollow(UUID followedUserId) {
        User user = getUserFromSecurityContext();

        if (followRepository.existsByFollowerIdAndFollowingId(user.getId(), followedUserId)) {
            followRepository.deleteByFollowerIdAndFollowingId(user.getId(), followedUserId);
            return;
        }

        User followingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotFoundException("user not found with id: " + user.getId()));

        User followedUser = userRepository.findById(followedUserId)
                    .orElseThrow(() -> new UserNotFoundException("user not found with id: " + followedUserId));

        Follow newFollowEntity = Follow.builder()
                    .follower(followingUser)
                    .following(followedUser)
                    .build();
        followRepository.save(newFollowEntity);
    }

}
