package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.PostMapper;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.request.RepostRequest;
import dev.thural.quietspace.model.request.VoteRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.repository.PostRepository;
import dev.thural.quietspace.repository.specifications.PostSpecifications;
import dev.thural.quietspace.service.PostService;
import dev.thural.quietspace.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildPageRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostSpecifications postSpecifications;
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostMapper postMapper;

    public final String AUTHOR_MISMATCH_MESSAGE = "post author mismatch with current user";

    @Override
    public Page<PostResponse> getAllPosts(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        return postRepository.findAll(postSpecifications.visibleToUser(), pageRequest)
                .map(postMapper::postEntityToResponse);
    }

    @Override
    public PostResponse addPost(PostRequest post) {
        User loggedUser = userService.getSignedUser();
        if (!loggedUser.getId().equals(post.getUserId())) throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
        return postMapper.postEntityToResponse(postRepository.save(postMapper.postRequestToEntity(post)));
    }

    public String getVotedPollOptionLabel(Poll poll) {
        UUID userId = userService.getSignedUser().getId();
        return poll.getOptions().stream()
                .filter(option -> option.getVotes().contains(userId))
                .findAny().map(PollOption::getLabel).orElse("not voted");
    }

    @Override
    public Optional<PostResponse> getPostById(UUID postId) {
        Post post = findPostEntityById(postId);
        return Optional.of(postMapper.postEntityToResponse(post));
    }

    @Override
    public PostResponse updatePost(UUID postId, PostRequest post) {
        User loggedUser = userService.getSignedUser();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (!postExistsByLoggedUser) throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
        BeanUtils.copyProperties(post, existingPost);
        return postMapper.postEntityToResponse(postRepository.save(existingPost));
    }

    @Override
    public PostResponse patchPost(UUID postId, PostRequest post) {
        User loggedUser = userService.getSignedUser();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (!postExistsByLoggedUser) throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
        if (StringUtils.hasText(post.getText())) existingPost.setText(post.getText());
        return postMapper.postEntityToResponse(postRepository.save(existingPost));
    }

    @Override
    @Transactional
    public void votePoll(VoteRequest voteRequest) {
        Post foundPost = postRepository.findById(voteRequest.getPostId()).orElseThrow(EntityNotFoundException::new);
        if (foundPost.getPoll().getOptions().stream().anyMatch(option -> option.getVotes().contains(voteRequest.getUserId())))
            return;
        foundPost.getPoll().getOptions().stream()
                .filter(option -> option.getLabel().equals(voteRequest.getOption())).findFirst()
                .ifPresent(option -> {
                    Set<UUID> votes = option.getVotes();
                    votes.add(voteRequest.getUserId());
                    option.setVotes(votes);
                });
    }

    @Override
    @Transactional
    public void deletePost(UUID postId) {
        User loggedUser = userService.getSignedUser();
        Post existingPost = findPostEntityById(postId);
        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPost, loggedUser);
        if (!postExistsByLoggedUser) throw new AccessDeniedException(AUTHOR_MISMATCH_MESSAGE);
        postRepository.deleteByRepostId(existingPost.getId().toString());
        postRepository.deleteById(postId);
    }

    @Override
    public Page<PostResponse> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);

        if (userId != null) {
            Specification<Post> specification = postSpecifications.combine(
                    postSpecifications.visibleToUser(),
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("user").get("id"), userId)
            );
            return postRepository.findAll(specification, pageRequest)
                    .map(postMapper::postEntityToResponse);
        } else {
            return postRepository.findAll(postSpecifications.visibleToUser(), pageRequest)
                    .map(postMapper::postEntityToResponse);
        }
    }

    @Override
    public Page<PostResponse> getAllByQuery(String searchText, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);

        Specification<Post> specification = postSpecifications.combine(
                postSpecifications.visibleToUser(),
                StringUtils.hasText(searchText) ? postSpecifications.containsText(searchText) : null
        );
        return postRepository.findAll(specification, pageRequest)
                .map(postMapper::postEntityToResponse);
    }

    @Override
    public PostResponse addRepost(RepostRequest repost) {
        return postMapper.postEntityToResponse(postRepository.save(postMapper.repostRequestToEntity(repost)));
    }

    @Override
    public Page<PostResponse> getSavedPostsByUser(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);
        UUID userId = userService.getSignedUser().getId();

        Specification<Post> specification = postSpecifications.combine(
                postSpecifications.visibleToUser(),
                postSpecifications.savedByUser(userId)
        );
        return postRepository.findAll(specification, pageRequest)
                .map(postMapper::postEntityToResponse);
    }

    @Override
    @Transactional
    public void savePostForUser(UUID postId) {
        Post foundPost = findPostEntityById(postId);
        userService.getSignedUser().getSavedPosts().add(foundPost);
    }

    @Override
    public Page<PostResponse> getCommentedPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, null);

        Specification<Post> specification = postSpecifications.combine(
                postSpecifications.visibleToUser(),
                postSpecifications.commentedByUser(userId)
        );
        return postRepository.findAll(specification, pageRequest)
                .map(postMapper::postEntityToResponse);
    }

    private boolean isPostExistsByLoggedUser(Post existingPost, User loggedUser) {
        return existingPost.getUser().equals(loggedUser);
    }

    private Post findPostEntityById(UUID postId) {
        return postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
    }
}
