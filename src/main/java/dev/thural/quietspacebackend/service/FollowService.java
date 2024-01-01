package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.FollowDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface FollowService {

    Page<FollowDTO> listFollowings(UUID userId, String jwtToken, Integer pageNumber, Integer pageSize);

    Page<FollowDTO> listFollowers(UUID userId, String jwtToken,  Integer pageNumber, Integer pageSize);

    void toggleFollow(UUID followingUserId, UUID followedUserId, String jwtToken);
}
