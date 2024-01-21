package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.exception.NotFoundException;
import dev.thural.quietspacebackend.entity.FollowEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.FollowMapper;
import dev.thural.quietspacebackend.model.FollowDTO;
import dev.thural.quietspacebackend.repository.FollowRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.FollowService;
import dev.thural.quietspacebackend.utils.JwtProvider;
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

    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;

    @Override
    public Page<FollowDTO> listFollowings(UUID userId, String jwtToken, Integer pageNumber, Integer pageSize) {
        checkUserValidity(userId, jwtToken);

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<FollowEntity> userPage = followRepository.findAllByFollowingId(userId, pageRequest);

        return userPage.map(followMapper::followEntityToDto);
    }

    @Override
    public Page<FollowDTO> listFollowers(UUID userId, String jwtToken, Integer pageNumber, Integer pageSize) {
        checkUserValidity(userId, jwtToken);

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<FollowEntity> userPage = followRepository.findAllByFollowerId(userId, pageRequest);

        return userPage.map(followMapper::followEntityToDto);
    }

    private void checkUserValidity(UUID userId, String jwtToken) {
        Optional<UserEntity> foundUser = jwtProvider.findUserByJwt(jwtToken);

        foundUser.ifPresent(userEntity -> {
            if (!userEntity.getId().equals(userId))
                throw new AccessDeniedException("current user has no access to the resource");
        });
    }

    @Override
    public void toggleFollow(UUID followingUserId, UUID followedUserId, String jwtToken) {
        checkUserValidity(followingUserId,jwtToken);

        if( followRepository.existsByFollowerIdAndFollowingId(followingUserId, followedUserId)){

            followRepository.deleteByFollowerIdAndFollowingId(followingUserId, followingUserId);
        } else {

            UserEntity followingUser = userRepository.findById(followingUserId).orElseThrow(NotFoundException::new);
            UserEntity followedUser = userRepository.findById(followingUserId).orElseThrow(NotFoundException::new);
            FollowEntity newFollowEntity = FollowEntity.builder().follower(followingUser).following(followedUser).build();

            followRepository.save(newFollowEntity);
        }
    }

}
