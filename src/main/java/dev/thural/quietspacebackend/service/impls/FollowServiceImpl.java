package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.FollowEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.FollowMapper;
import dev.thural.quietspacebackend.model.FollowDTO;
import dev.thural.quietspacebackend.repository.FollowRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.FollowService;
import dev.thural.quietspacebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspacebackend.utils.CustomPageProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    private final FollowRepository followRepository;

    private final UserService userService;

    private final UserRepository userRepository;

    @Override
    public Page<FollowDTO> listFollowings(UUID userId, String authHeader, Integer pageNumber, Integer pageSize) {
        checkUserValidity(userId, authHeader);

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<FollowEntity> userPage = followRepository.findAllByFollowingId(userId, pageRequest);

        return userPage.map(followMapper::followEntityToDto);
    }

    @Override
    public Page<FollowDTO> listFollowers(UUID userId, String authHeader, Integer pageNumber, Integer pageSize) {
        checkUserValidity(userId, authHeader);

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<FollowEntity> userPage = followRepository.findAllByFollowerId(userId, pageRequest);

        return userPage.map(followMapper::followEntityToDto);
    }

    private void checkUserValidity(UUID userId, String jwtToken) {
        Optional<UserEntity> foundUser = userService.findUserByJwt(jwtToken);

        foundUser.ifPresent(userEntity -> {
            if (!userEntity.getId().equals(userId))
                throw new AccessDeniedException("current user has no access to the resource");
        });
    }

    @Override
    public void toggleFollow(UUID followingUserId, UUID followedUserId, String authHeader) {
        checkUserValidity(followingUserId, authHeader);

        if (followRepository.existsByFollowerIdAndFollowingId(followingUserId, followedUserId)) {

            followRepository.deleteByFollowerIdAndFollowingId(followingUserId, followingUserId);
        } else {

            UserEntity followingUser = userRepository.findById(followingUserId)
                    .orElseThrow(() -> new UserNotFoundException("user not found"));
            UserEntity followedUser = userRepository.findById(followingUserId)
                    .orElseThrow(() -> new UserNotFoundException("user not found"));
            FollowEntity newFollowEntity = FollowEntity.builder().follower(followingUser).following(followedUser).build();

            followRepository.save(newFollowEntity);
        }
    }

}
