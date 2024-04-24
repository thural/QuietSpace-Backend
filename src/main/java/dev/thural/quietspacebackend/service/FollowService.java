package dev.thural.quietspacebackend.service;

import dev.thural.quietspacebackend.model.response.FollowResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface FollowService {

    Page<FollowResponse> listFollowings(Integer pageNumber, Integer pageSize);

    Page<FollowResponse> listFollowers(Integer pageNumber, Integer pageSize);

    void toggleFollow(UUID followedUserId);
}
