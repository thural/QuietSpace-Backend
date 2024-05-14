package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostService {

    Page<PostResponse> getAllPosts(Integer pageNumber, Integer pageSize);

    void addPost(PostRequest post);

    Optional<PostResponse> getPostById(UUID id);

    void updatePost(UUID id, PostRequest post);

    void deletePost(UUID id);

    void patchPost(UUID id, PostRequest post);

    void votePostPoll(VoteRequest voteRequest);

    String getVotedPollOptionLabel(Poll poll);

    Page<PostResponse> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize);

    List<ReactionResponse> getPostLikesByPostId(UUID postId);

}
