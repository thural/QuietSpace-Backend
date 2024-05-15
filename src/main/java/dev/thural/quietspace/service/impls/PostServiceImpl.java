package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.*;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.ReactionMapper;
import dev.thural.quietspace.mapper.custom.PostMapper;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.repository.ReactionRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static dev.thural.quietspace.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ReactionMapper reactionMapper;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    public final String AUTHOR_MISMATCH_MESSAGE = "post author mismatch with current user";

    @Override
    public Page<PostResponse> getAllPosts(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        return postRepository.findAll(pageRequest).map(postMapper::postEntityToResponse);
    }

    @Override
    public void addPost(PostRequest post) {
        User loggedUser = getUserFromSecurityContext();
        if (!loggedUser.getId().equals(post.getUserId()))
            throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
        Post postEntity = postMapper.postRequestToEntity(post);
        postEntity.setUser(loggedUser);
        postRepository.save(postEntity);
    }

    public String getVotedPollOptionLabel(Poll poll){
        UUID userId = getUserFromSecurityContext().getId();

        return poll.getOptions().stream()
               .filter(option -> option.getVotes().contains(userId))
               .findAny()
                .map(PollOption::getLabel).orElse("not voted");
    }

    private User getUserFromSecurityContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
    }

    @Override
    public Optional<PostResponse> getPostById(UUID postId) {
        Post post = findPostEntityById(postId);
        PostResponse postResponse = postMapper.postEntityToResponse(post);
        return Optional.of(postResponse);
    }

    @Override
    public void updatePost(UUID postId, PostRequest post) {
        User loggedUser = getUserFromSecurityContext();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (postExistsByLoggedUser) {
            existingPost.setText(post.getText());
            postRepository.save(existingPost);
        } else throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
    }

    @Override
    public void patchPost(UUID postId, PostRequest post) {
        User loggedUser = getUserFromSecurityContext();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (postExistsByLoggedUser) {
            if (StringUtils.hasText(post.getText())) existingPost.setText(post.getText());
            postRepository.save(existingPost);
        } else throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
    }

    @Override
    public void votePostPoll(VoteRequest voteRequest) {
        Post foundPost = postRepository.findById(voteRequest.getPostId())
                .orElseThrow(EntityNotFoundException::new);

        foundPost.getPoll().getOptions()
                .stream().filter(option -> option.getLabel().equals(voteRequest.getOption()))
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
        User loggedUser = getUserFromSecurityContext();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (postExistsByLoggedUser) postRepository.deleteById(postId);
        else throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
    }

    @Override
    public Page<PostResponse> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);
        Page<Post> postPage;
        if (userId != null) {
            postPage = postRepository.findAllByUserId(userId, pageRequest);
        } else {
            postPage = postRepository.findAll(pageRequest);
        }
        return postPage.map(postMapper::postEntityToResponse);
    }

    @Override
    public List<ReactionResponse> getPostLikesByPostId(UUID postId) {
        return reactionRepository.findAllByContentId(postId).stream()
                .map(reactionMapper::reactionEntityToResponse)
                .toList();
    }

    private boolean isPostExistsByLoggedUser(Post existingPost, User loggedUser) {
        return existingPost.getUser().equals(loggedUser);
    }

    private Post findPostEntityById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);
    }

}
