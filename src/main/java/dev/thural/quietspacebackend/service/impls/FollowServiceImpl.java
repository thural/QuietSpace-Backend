package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.FollowEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.mapper.FollowMapper;
import dev.thural.quietspacebackend.model.FollowDto;
import dev.thural.quietspacebackend.repository.FollowRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static dev.thural.quietspacebackend.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    public Page<FollowDto> listFollowings(Integer pageNumber, Integer pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        Page<FollowEntity> userPage = followRepository.findAllByFollowingId(user.getId(), pageRequest);

        return userPage.map(followMapper::followEntityToDto);
    }

    @Override
    public Page<FollowDto> listFollowers(Integer pageNumber, Integer pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        Page<FollowEntity> userPage = followRepository.findAllByFollowerId(user.getId(), pageRequest);

        return userPage.map(followMapper::followEntityToDto);
    }

    @Override
    public void toggleFollow(UUID followedUserId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (followRepository.existsByFollowerIdAndFollowingId(user.getId(), followedUserId)) {
            followRepository.deleteByFollowerIdAndFollowingId(user.getId(), followedUserId);
        } else {
            UserEntity followingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotFoundException("user not found"));
            UserEntity followedUser = userRepository.findById(followedUserId)
                    .orElseThrow(() -> new UserNotFoundException("user not found"));

            FollowEntity newFollowEntity = FollowEntity.builder()
                    .follower(followingUser)
                    .following(followedUser)
                    .build();
            followRepository.save(newFollowEntity);
        }
    }

}
