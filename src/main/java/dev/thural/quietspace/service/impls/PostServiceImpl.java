package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.*;
import dev.thural.quietspace.mapper.custom.PostMapper;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostMapper postMapper;

    public final String AUTHOR_MISMATCH_MESSAGE = "post author mismatch with current user";

    @Override
    public Page<PostResponse> getAllPosts(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        return postRepository.findAll(pageRequest).map(postMapper::postEntityToResponse);
    }

    @Override
    public PostResponse addPost(PostRequest post) {
        User loggedUser = userService.getLoggedUser();
        if (!loggedUser.getId().equals(post.getUserId()))
            throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
        return postMapper.postEntityToResponse(
                postRepository.save(postMapper.postRequestToEntity(post))
        );
    }

    public String getVotedPollOptionLabel(Poll poll){
        UUID userId = userService.getLoggedUser().getId();

        return poll.getOptions().stream()
               .filter(option -> option.getVotes().contains(userId))
               .findAny()
                .map(PollOption::getLabel).orElse("not voted");
    }

    @Override
    public Optional<PostResponse> getPostById(UUID postId) {
        Post post = findPostEntityById(postId);
        return Optional.of(postMapper.postEntityToResponse(post));
    }

    @Override
    public PostResponse updatePost(UUID postId, PostRequest post) {
        User loggedUser = userService.getLoggedUser();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (postExistsByLoggedUser) {
            existingPost.setText(post.getText());
            return postMapper.postEntityToResponse(postRepository.save(existingPost));
        } else throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
    }

    @Override
    public PostResponse patchPost(UUID postId, PostRequest post) {
        User loggedUser = userService.getLoggedUser();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (postExistsByLoggedUser) {
            if (StringUtils.hasText(post.getText())) existingPost.setText(post.getText());
            return postMapper.postEntityToResponse(postRepository.save(existingPost));
        } else throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
    }

    @Override
    public void votetPoll(VoteRequest voteRequest) {
        Post foundPost = postRepository.findById(voteRequest.getPostId())
                .orElseThrow(EntityNotFoundException::new);

        if(foundPost.getPoll().getOptions().stream()
                .anyMatch(option -> option.getVotes().contains(voteRequest.getUserId()))) return;

        foundPost.getPoll().getOptions().stream()
                .filter(option -> option.getLabel().equals(voteRequest.getOption()))
                .findFirst()
                .ifPresent(option -> {
                    Set<UUID> votes = option.getVotes();
                    votes.add(voteRequest.getUserId());
                    option.setVotes(votes);
                });

        postRepository.save(foundPost);
    }

    @Override
    public void deletePost(UUID postId) {
        User loggedUser = userService.getLoggedUser();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (postExistsByLoggedUser) postRepository.deleteById(postId);
        else throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
    }

    @Override
    public Page<PostResponse> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        Page<Post> postPage;
        if (userId != null) {
            postPage = postRepository.findAllByUserId(userId, pageRequest);
        } else {
            postPage = postRepository.findAll(pageRequest);
        }
        return postPage.map(postMapper::postEntityToResponse);
    }

    private boolean isPostExistsByLoggedUser(Post existingPost, User loggedUser) {
        return existingPost.getUser().equals(loggedUser);
    }

    private Post findPostEntityById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);
    }

}
