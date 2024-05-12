package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.*;
import dev.thural.quietspace.exception.UserNotFoundException;
import dev.thural.quietspace.mapper.PostLikeMapper;
import dev.thural.quietspace.model.request.PollRequest;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.model.response.PostLikeResponse;
import dev.thural.quietspace.repository.PostLikeRepository;
import dev.thural.quietspace.repository.UserRepository;
import dev.thural.quietspace.mapper.PostMapper;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

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
        Post newPost = postMapper.postRequestToEntity(post);
        newPost.setUser(loggedUser);

        if(post.getPoll() != null) {
            PollRequest pollRequest = post.getPoll();

            Poll newPoll = Poll.builder()
                    .post(newPost)
                    .dueDate(pollRequest.getDueDate())
                    .build();

            List<PollOption> options = pollRequest.getOptions().stream()
                    .map(option -> PollOption.builder()
                            .label(option)
                            .poll(newPoll)
                            .votes(List.of())
                            .build())
                    .toList();

            newPoll.setOptions(options);

            newPost.setPoll(newPoll);
        }

        postRepository.save(newPost);
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
    public List<PostLikeResponse> getPostLikesByPostId(UUID postId) {
        return postLikeRepository.findAllByPostId(postId).stream()
                .map(postLikeMapper::postLikeEntityToResponse)
                .toList();
    }

    @Override
    public List<PostLikeResponse> getPostLikesByUserId(UUID userId) {
        return postLikeRepository.findAllByUserId(userId).stream()
                .map(postLikeMapper::postLikeEntityToResponse)
                .toList();
    }

    @Override
    public void togglePostLike(UUID postId) {
        User user = getUserFromSecurityContext();
        boolean isPostLikeExists = postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
        if (isPostLikeExists) postLikeRepository.deleteById(postId);
        else {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("post not found"));
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
        }
    }

    private boolean isPostExistsByLoggedUser(Post existingPost, User loggedUser) {
        return existingPost.getUser().equals(loggedUser);
    }

    private Post findPostEntityById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);
    }

}
