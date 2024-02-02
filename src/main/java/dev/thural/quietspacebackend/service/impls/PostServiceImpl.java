package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.exception.UserNotFoundException;
import dev.thural.quietspacebackend.model.PostDto;
import dev.thural.quietspacebackend.model.PostLikeDto;
import dev.thural.quietspacebackend.repository.PostLikeRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.UserService;
import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.PostMapper;
import dev.thural.quietspacebackend.repository.PostRepository;
import dev.thural.quietspacebackend.service.PostService;
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

import static dev.thural.quietspacebackend.utils.PagingProvider.buildCustomPageRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserService userService;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    @Override
    public Page<PostDto> getAllPosts(Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        return postRepository.findAll(pageRequest).map(postMapper::postEntityToDto);
    }

    @Override
    public PostDto addPost(PostDto post) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        PostEntity postEntity = postMapper.postDtoToEntity(post);

        postEntity.setUser(loggedUser);
        return postMapper.postEntityToDto(postRepository.save(postEntity));
    }

    @Override
    public Optional<PostDto> getPostById(UUID postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        PostDto postDTO = postMapper.postEntityToDto(postEntity);
        return Optional.of(postDTO);
    }

    @Override
    public void updatePost(UUID postId, PostDto post) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        PostEntity existingPostEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUser);

        if (postExistsByLoggedUser) {
            existingPostEntity.setText(post.getText());
            postRepository.save(existingPostEntity);
        } else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public void patchPost(UUID postId, PostDto post) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        PostEntity existingPostEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUser);

        if (postExistsByLoggedUser) {
            if (StringUtils.hasText(post.getText())) existingPostEntity.setText(post.getText());
            postRepository.save(existingPostEntity);
        } else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public void deletePost(UUID postId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity loggedUser = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        PostEntity existingPostEntity = postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new);

        boolean postExistsByLoggedUser = isPostExistsByLoggedUser(existingPostEntity, loggedUser);

        if (postExistsByLoggedUser) postRepository.deleteById(postId);
        else throw new AccessDeniedException("post author does not belong to current user");
    }

    @Override
    public Page<PostDto> getPostsByUserId(UUID userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildCustomPageRequest(pageNumber, pageSize);

        Page<PostEntity> postPage;

        if (userId != null) {
            postPage = postRepository.findAllByUserId(userId, pageRequest);
        } else {
            postPage = postRepository.findAll(pageRequest);
        }

        return postPage.map(postMapper::postEntityToDto);
    }

    private boolean isPostExistsByLoggedUser(PostEntity existingPostEntity, UserEntity loggedUser) {
        return existingPostEntity.getUser().equals(loggedUser);
    }

    @Override
    public List<PostLikeDto> getPostLikesByPostId(UUID postId) {
        return postLikeRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostLikeDto> getPostLikesByUserId(UUID userId) {
        return postLikeRepository.findAllByUserId(userId);
    }

    @Override
    public void togglePostLike(UUID postId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        boolean isPostLikeExists = postLikeRepository.existsByPostIdAndUserId(postId, userEntity.getId());

        if (isPostLikeExists) postLikeRepository.deleteById(postId);
        else {
            PostEntity postEntity = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("post not found"));
            postLikeRepository.save(PostLikeEntity.builder().post(postEntity).user(userEntity).build());
        }
    }

}
